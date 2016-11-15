/*
 * <b>Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.imgtec.creator.sniffles.data;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NsdServiceImpl implements NsdService {

  private static final String SERVICE_TYPE = "_onboarding._tcp";
  private static final int DELAY = 10;

  private final Object lock = new Object();
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final NsdManager nsdManager;
  private final ScheduledExecutorService executorService;
  private final Handler mainHandler;

  private CopyOnWriteArraySet<DiscoveryServiceListener> listeners = new CopyOnWriteArraySet<>();

  boolean isDiscovering = false;
  Stack<NsdServiceInfo> stack;
  Resolver resolver;

  NsdServiceImpl(Context appContext, ScheduledExecutorService executorService, Handler mainHandler) {
    this.nsdManager = (NsdManager) appContext.getSystemService(Context.NSD_SERVICE);
    this.executorService = executorService;
    this.mainHandler = mainHandler;
  }

  @Override
  public void addDiscoveryServiceListener(DiscoveryServiceListener l) {
    listeners.add(l);
  }

  @Override
  public void removeDiscoveryServiceListener(DiscoveryServiceListener l) {
    listeners.remove(l);
  }

  @Override
  public void discoverServices() {
    try {
      synchronized (lock) {
        if (!isDiscovering) {
          isDiscovering = true;
          nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
          scheduleStopDiscoveryTask();

          stack = new Stack<>();

          tryReleaseResolver();

          resolver = new Resolver(stack, this);
          resolver.start();
        }
      }
    }
    catch (Exception e) {
      logger.warn("Discover services failed!", e);
    }
  }

  private void tryReleaseResolver() {

    if (resolver != null) {
      resolver.interrupt();
      resolver = null;
    }
  }

  private void scheduleStopDiscoveryTask() {
    executorService.schedule(new Runnable() {
      @Override
      public void run() {
        logger.debug("Stopping discovery task");
        tryStopDiscovery();
      }
    }, DELAY, TimeUnit.SECONDS);
  }

  @Override
  public void stopDiscovery() {
    tryStopDiscovery();
  }

  void tryStopDiscovery() {
    try {
      synchronized (lock) {
        if (isDiscovering) {
          isDiscovering = false;
          nsdManager.stopServiceDiscovery(discoveryListener);
        }

        tryReleaseResolver();
      }
    } catch (Exception e) {
      logger.warn("Stop service discovery failed!", e);
    }
  }

  NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
      logger.warn("Start discovery failed! ServiceType {}, errorCode {}", serviceType, errorCode);
      tryStopDiscovery();
      notifyDiscoveryFailed();
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
      logger.warn("Stop discovery failed! ServiceType {}, errorCode {}", serviceType, errorCode);
      tryStopDiscovery();
      notifyDiscoveryFailed();
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
      logger.debug("Discovery started. ServiceType {}", serviceType);
      notifyDiscoveryStarted(serviceType);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
      logger.debug("Discovery stopped. ServiceType {}", serviceType);
      notifyDiscoveryStopped(serviceType);
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
      logger.debug("Service found: ServiceType {}", serviceInfo);
      synchronized (stack) {
        stack.add(serviceInfo);
        stack.notifyAll();
      }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
      logger.debug("Service lost. ServiceType {}", serviceInfo);
    }
  };

  private void notifyDiscoveryFailed() {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {

        for (DiscoveryServiceListener l : listeners) {
          l.onDiscoveryFailed(NsdServiceImpl.this);
        }
      }
    });
  }

  private void notifyDiscoveryStarted(final String serviceType) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {

        for (DiscoveryServiceListener l : listeners) {
          l.onDiscoveryStarted(NsdServiceImpl.this, serviceType);
        }
      }
    });
  }

  private void notifyDiscoveryStopped(final String serviceType) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {

        for (DiscoveryServiceListener l : listeners) {
          l.onDiscoveryFinished(NsdServiceImpl.this, serviceType);
        }
      }
    });
  }

  static class Resolver extends Thread {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final Stack<NsdServiceInfo> stack;
    final NsdServiceImpl service;

    Resolver(Stack<NsdServiceInfo> stack, NsdServiceImpl service) {
      super();
      this.stack = stack;
      this.service = service;
      this.setName("DNS Resolver");
    }

    @Override
    public void run() {
      logger.info("Starting Resolver thread");
      final boolean[] resolved = {false};

      while (!isInterrupted()) {
        try {
          synchronized (stack) {
            while (stack.size() == 0) {
              stack.wait();
            }
          }

          NsdServiceInfo serviceInfo = stack.pop();
          synchronized (stack) {
            resolved[0] = false;
          }

          logger.debug("Resolving discovered service: {}", serviceInfo);
          service.nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(final NsdServiceInfo serviceInfo, final int errorCode) {
              logger.debug("Resolve failed: {}, {}", serviceInfo, errorCode);
              synchronized (stack) {
                resolved[0] = true;
                stack.notifyAll();
              }

              for (DiscoveryServiceListener l : service.listeners) {
                l.onServiceLost(service, new ServiceInfo(serviceInfo), errorCode);
              }
            }

            @Override
            public void onServiceResolved(final NsdServiceInfo serviceInfo) {
              logger.debug("Service resolved: {}", serviceInfo);
              synchronized (stack) {
                resolved[0] = true;
                stack.notifyAll();
              }

              for (DiscoveryServiceListener l : service.listeners) {
                l.onServiceFound(service, new ServiceInfo(serviceInfo));
              }
            }
          });

          synchronized (stack) {
            while(!resolved[0]) {
              stack.wait();
            }
          }

        } catch (InterruptedException e) {
          logger.warn("Resolver interrupted while waiting");
          break;
        }
      }
      logger.info("Resolver thread finished");
    }
  }
}
