/*
 * This file is part of ToolStats.
 *
 * ToolStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ToolStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToolStats.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.toolstats.tools.config;

import lol.hyper.toolstats.ToolStats;
import lol.hyper.toolstats.tools.config.versions.*;

public class ConfigUpdater {

    private final ToolStats toolStats;

    public ConfigUpdater(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void updateConfig() {
        int version = toolStats.config.getInt("config-version");

        // Version 5 to 6
        if (version == 5) {
            Version6 version6 = new Version6(toolStats);
            version6.update();
        }
        // Version 6 to 7
        if (version == 6) {
            Version7 version7 = new Version7(toolStats);
            version7.update();
        }
        // Version 7 to 8
        if (version == 7) {
            Version8 version8 = new Version8(toolStats);
            version8.update();
        }
        // Version 8 to 9
        if (version == 8) {
            Version9 version9 = new Version9(toolStats);
            version9.update();
        }
        // Version 9 to 10
        if (version == 9) {
            Version10 version10 = new Version10(toolStats);
            version10.update();
        }
    }
}
