package scoutcert

import scoutcert.menu.MenuItem
import scoutcert.menu.MainMenuItem
import scoutcert.menu.SubMenuItem

class MenuTagLib {

    MenuService menuService

    def menu = {attrs->
        out << render(template: "/layouts/menu", model: [menuItems: menuService.menu])
    }

    def menuItem = {attrs->

        String currController = attrs.controller;
        String currAction = attrs.action;

        MenuItem menuItem = attrs.menuItem;
        String isCurr = (menuItem.isCurrentMenuItem(currController, currAction)) ? "class='on'" : "";

        out << "<li ${isCurr}>${link(controller: menuItem.controller, action: menuItem.action ?: "index") { message(code: menuItem.labelCode)}}</li>"
    }
    
    def subMenu = {attrs->

        //Find currently selected main menu items
        menuService.menu?.each{MainMenuItem mainMenuItem ->
            if(mainMenuItem.isCurrentMenuItem(attrs.controller, attrs.action) && mainMenuItem.subItems?.size() > 0) {
                out << '<div id="top-nav"><ul>'
                mainMenuItem.subItems?.each {
                    SubMenuItem subItem->
                    out << menuItem(controller: attrs.controller, action: attrs.controller, menuItem: subItem)
                }
                out << '</ul></div>'
            }
        }
    }

}
