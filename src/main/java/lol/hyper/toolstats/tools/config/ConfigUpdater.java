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
import lol.hyper.toolstats.tools.config.versions.Version6;
import lol.hyper.toolstats.tools.config.versions.Version7;
import lol.hyper.toolstats.tools.config.versions.Version8;

public class ConfigUpdater {

    private final ToolStats toolStats;

    public ConfigUpdater(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void updateConfig() {
        int version = toolStats.config.getInt("config-version");

        switch(version) {
            case 5: {
                // Version 5 to 6
                Version6 version6 = new Version6(toolStats);
                version6.update();
                break;
            }
            case 6: {
                // Version 6 to 7
                Version7 version7 = new Version7(toolStats);
                version7.update();
                break;
            }
            case 7: {
                // Version 7 to 8
                Version8 version8 = new Version8(toolStats);
                version8.update();
                break;
            }
        }
    }
}
