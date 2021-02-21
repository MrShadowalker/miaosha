/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.common.push.supplier;

/**
 * @author zebin.xuzb 2013-1-23 下午5:08:51
 * @since 4.1.3
 */
public abstract class AbstractDatasourceSupplier implements DatasourceSupplier {

    private Object lock = new Object();
    protected volatile boolean running = false;

    @Override
    public void start() {
        synchronized (lock) {
            if (isStart()) {
                return;
            }
            doStart();
            running = true;
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (!isStart()) {
                return;
            }
            doStop();
            running = false;
        }
    }

    @Override
    public boolean isStart() {
        return running;
    }

    protected abstract void doStart();

    protected abstract void doStop();
}
