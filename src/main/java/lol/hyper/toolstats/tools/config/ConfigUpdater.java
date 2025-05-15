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

        switch (version) {
            case 5 -> new Version6(toolStats).update(); // 5 to 6
            case 6 -> new Version7(toolStats).update(); // 6 to 7
            case 7 -> new Version8(toolStats).update(); // 7 to 8
            case 8 -> new Version9(toolStats).update(); // 8 to 9
            case 9 -> new Version10(toolStats).update(); // 9 to 10
            case 10 -> new Version11(toolStats).update(); // 10 to 11
            case 11 -> new Version12(toolStats).update(); // 11 to 12
            case 12 -> new Version13(toolStats).update(); // 12 to 13
        }
    }
}