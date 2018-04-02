/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.runtime.tools.logging;

import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Extended version of {@link ThreadConverter} which trims the OSGi ConfigAdmin update thread name.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ExtThreadConverter extends ThreadConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String threadName = super.convert(event);
        if (StringUtils.startsWith(threadName, "CM Event Dispatcher")) {
            return "CM Event Dispatcher";
        } else if (StringUtils.startsWith(threadName, "Background Update")) {
            return "Background Update";
        }
        return threadName;
    }
}
