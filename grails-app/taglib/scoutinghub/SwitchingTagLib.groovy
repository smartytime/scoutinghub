package scoutinghub

import scoutinghub.menu.MainMenuItem
import scoutinghub.menu.SubMenuItem
import grails.plugins.springsecurity.SpringSecurityService

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * This class will provide switching between mobile and regular tags
 */
class SwitchingTagLib {
    MenuService menuService

    SpringSecurityService springSecurityService

    static namespace = "s"

    def iconMap = [training: "check", unit: "home"]

    def propertyList = { attrs, body ->
        if (session.isMobile) {
            out << body()
        } else {
            out << "<div class='property-list ${attrs.class ?: ""}'>${body()}</div>"
//            out << body()
        }

    }

    def address = { attrs, body ->
        Address addr = attrs.address
        if (session.isMobile) {
//            out << "<li class='smallfield'><span class='name'>${message(code: attrs.code)}</span><span class='value'>${body()}</span></li>"
        } else {
            out << "<div class='fldData'>${addr.locationName}</div>"
            out << "<div class='addressProperty'>${addr.address}</div>";
            out << "<div class='addressProperty'>${addr.city} ${addr.state} ${addr.zip}</div>";
            out << "</span>"
        }
    }

    def mobile = { attrs, body ->
        if (session.isMobile) {
            out << body()
        }
    }

    def browser = { attrs, body ->
        if (!session.isMobile) {
            out << body()
        }
    }

    def mapLink = { attrs, body ->
        if (session.isMobile) {
            //todo: Implement this!
        } else {
            Address addr = attrs.address
            String addrString = "${addr.address} ${addr.city}, ${addr.state} ${addr.zip}"
            out << "<a href=\"javascript:showMap('${addrString}')\">${addrString}</a>"
        }
    }

    def tooltip = { attrs ->
        def code = attrs.code
        def selector = attrs.selector
        out << r.script() {
            out << "if(createTooltip) createTooltip('${selector}', '${message(code: code)}', '${attrs.onEvent ?: ''}', '${attrs.offEvent ?: ''}');"
        }
    }

    def leaderUnit = { attrs, body ->
        LeaderGroup leaderGroup = attrs.leaderGroup;

        def groupName = message(code:attrs.code)
        Leader leader = leaderGroup.leader

        String unitName = body()
        if(leaderGroup?.scoutGroup?.canBeAdministeredBy(leader)) {
            unitName += " (admin)"
        }

        def model = [:]
        model.leaderGroup = leaderGroup
        model.unitName=unitName
        model.groupName=groupName

        out << render(template:"/leaderGroup/display", model: model)

    }

    def property = { attrs, body ->

//        out << "<li class='prop ${attrs.class ?: ''}'>"
        out << "<div class=\"control-group row-fluid ${attrs.class}\">\n"
        out << "<div class=\"span6 label-span\">"
        out << "    <label class=\"control-label\" for=\"${attrs.name}\"><h4>${message(code: attrs.code, args: attrs.args)}</h4></label>\n"
        out << "</div>"
        out << "    <div class=\"span6 controls ${attrs.text ? "field-text" : ""}\">\n"
        out << "<h5>"
        out << body()
        out << "</h5>"
        out << "    </div>\n"
        out << "</div>\n"

    }

    def pageItem = { attrs, body ->
        if (session.isMobile) {
            out << "<li class='${attrs.type}'><span class='name'>${message(code: attrs.name)}</span>${body()}</li>"
        } else {
            out << "<div class='${attrs.type}'><span class='name'>${message(code: attrs.name)}</span>${body()}</div>"
        }
    }

    def item = { attrs, body ->
        if (session.isMobile) {
            out << "<li class='textbox'>${body()}</li>"
        } else {
            out << "<div class='headerText'>${body()}</div>"
        }
    }

    def content = { attrs, body ->
        if (attrs.small) {
            attrs.class = "offset2"
            attrs.span = "8"
        }

        if (attrs.med) {
            attrs.class = "offset1"
            attrs.span = "10"
        }

        attrs.row = "true"
        out << boxed(attrs, body)
//        if (session.isMobile) {
//            out << iwebkit.content(attrs, body)
//        } else {
//            out << "<div class='content ${attrs.class ?: ""}'>"
//            out << body()
////            out << "<div style='clear:both;'></div>"
//            out << "</div>"
//        }

    }

    def text = { attrs, body ->
        if (session.isMobile) {
            out << "<li class='textbox'>"
            out << body()
            out << "</li>"
        } else {
            out << "<div class='text ${attrs.class ?: ''}'>${body()}</div>"

        }
    }

    def smallHeader = { attrs, body ->
        if (session.isMobile) {
            out << "<span class='graytitle'>${body()}</span>"
        } else {
            out << "<h2>${body()}</h2>"
        }
    }

    def sectionHeader = { attrs, body ->
        def icon = attrs.icon

        icon = iconMap[icon] ?: icon

        def args = attrs.args

//        out << """
//                <div class="content-box-header">
//                    <i class="icon-$icon"></i> ${g.message(code:attrs.code, default:attrs.code, args:args)}
//                </div>
//
//"""

        out << "<h2>"
        if (icon) {
            out << "<i class=\"icon-$icon\"></i> "
        }

        out << g.message(code: attrs.code, default: attrs.code, args: args)
        out << "</h2>"
        out << body()

//        if (session.isMobile) {
//            g.set(var: "sectionHeader", value: message(code: attrs.code, default: attrs.code, args: args), scope: "request");
//            //evaluate the body, but don't render it
//            out << body()
//        } else {
//            out << g.header(attrs, body)
//        }
    }

    def form = { attrs, body ->
        out << body()
    }

    def leaderTraining = { attrs ->
        out << f.leaderTraining(certificationInfo: attrs.certificationInfo)
    }

    def trainingRollup = { attrs ->
        String msg = attrs.message
        int pct = attrs.pct
        String controller = attrs.controller
        String action = attrs.action
        String typeCode = attrs.typeCode
        def id = attrs.id

        if (session.isMobile) {
            out << linker(controller: controller, action: action, id: id, code: 'training.completion',
                    args: [pct]) {
                out << "${msg?.trimTo(24)}"
            }
        } else {

            out << "<div class='training-report-item'>"
            out << "<div class=\"training-report-name\">"
            out << link(controller: controller, action: action, id: id) {
                out << "${msg}"
            }
            out << "</div>"
            out << "<div class='training-report-type'>"
            out << message(code: typeCode)
            out << "</div>"
            out << "<div class='training-report-data'><div class=\"progress\" value=\"${pct}\"></div></div>"
            out << "</div>"
        }
    }
    def leaderTrainingRollup = { attrs ->
        LeaderGroup leaderGroup = attrs.leaderGroup
        if (session.isMobile) {
            out << linker(controller: "leader", action: "view", id: leaderGroup?.leader?.id, code: 'training.completion',
                    args: [(int) leaderGroup?.pctTrained]) {
                out << "${leaderGroup.leader.toString()?.trimTo(24)}"
            }
        } else {
            def message = "${leaderGroup.leader}"
            out << trainingRollup(message: message, pct: (int) leaderGroup?.pctTrained, controller: "leader",
                    action: "view", id: leaderGroup.leader.id, typeCode: "${leaderGroup.leaderPosition}.label")
        }
    }

    def submit = { attrs, body ->
        def name = attrs.name
        def value = attrs.value
//        if (session.isMobile) {
//            out << "<li class='button'>"
//            out << submitButton(attrs)
//            out << "</li>"
//        } else {
//
//        }

        attrs.class += " btn btn-primary"
        out << submitButton(attrs)

    }

    def checkbox = { attrs, body ->
        if (session.isMobile) {

            out << """<li class='checkbox ${attrs.class ?: ''}'>
                    <span class='name'>${message(code: attrs.code)}</span>"""
            out << g.checkBox(attrs)
            out << "</li>"

        } else {
            out << "<div class='${attrs?.class}'>"
            out << "<span class='chk-input'>"
            String name = attrs.id ?: attrs.name
            out << g.checkBox(attrs)
            out << "</span>"
            out << "<span class='chk-label'><label for='$name'><h4>"
            out << message(code: attrs?.code)
            out << "</h4></label></span>"
            out << "</div>"
        }
    }

    def msg = { attrs, body ->
        def code = attrs.code
        def code2 = attrs.code2

        out << "<div class=\"alert alert-${attrs.type}\">"
        if (attrs.code) {
            out << message(code: attrs.code)
        } else {
            out << body()
        }
        out << "</div>"

//        if (session.isMobile) {
//            out << item {
//                out << "<div class='${attrs.type}'>"
//                if (attrs.code) {
//                    out <<
//                }
//                out << body()
//                out << "</div>"
//            }
//        } else {
////            attrs.code2 = attrs.code
//            //            attrs.code = null
//            out << msgbox(attrs, body)
//        }
    }

    def menu = { attrs, body ->
        if (session.isMobile) {
            menuService.menu?.each {
                MainMenuItem menuItem ->

                    //First, build a list of subItems that would
                    //be rendered
                    List<SubMenuItem> toRender = [];
                    menuItem.subItems?.each { SubMenuItem subItem ->
                        if (subItem.requiredRoles?.size() > 0) {
                            if (SpringSecurityUtils.ifAllGranted(subItem.requiredRoles?.join())) {
                                toRender << subItem
                            }
                        } else {
                            toRender << subItem
                        }
                    }


                    if (toRender?.size() > 0) {


                        out << section(code: menuItem.labelCode) {
                            toRender?.each { SubMenuItem subItem ->
                                attrs.controller = subItem.controller
                                attrs.action = subItem.action
                                out << linker(attrs) {
                                    out << message(code: subItem.labelCode)
                                }
                            }
                        }
                    }
            }


        } else {
            //out << menuItem(attrs, body)
        }

    }

    def linker = { attrs, body ->
        if(attrs.menu) {
            attrs.class += " btn btn-small"
        }
        if (session.isMobile) {
            out << "<li class='menu linker'>"
            def bodyClosure = {
                if (attrs.img) {
                    String img = attrs.img
                    if (!img?.contains("images")) {
                        img = "/scoutinghub/images/${img}.png"
                    }
                    out << "<img src='${img}' />"
                }
                out << "<span class='name'>${body()}</span>"
                if (attrs.comment) {
                    out << "<span class='comment'>${attrs.comment}</span>"
                } else if (attrs.code) {
                    out << "<span class='comment'>${message(code: attrs.code, args: attrs.args)}</span>"
                }
                out << "<span class='arrow'></span>"
            }

            if (attrs.controller || attrs.action) {
                out << link(attrs, bodyClosure)
            } else {
                out << bodyClosure()
            }

            out << "</li>"
        } else {
            out << link(attrs, body)
        }
    }

    def div = { attrs, body ->
        if (session.isMobile) {
            out << body()
        } else {
            out << "<div style='${attrs.style ?: ''}' class='${attrs.class ?: ''}'>${body()}</div>"
        }

    }


    def bigButton = { attrs, body ->
//        if (session.isMobile) {
//            out << linker(attrs) {
//                out << body()
//            }
//        } else {
//            out << "<div class='big-button-container'>"
//            attrs.class = "${attrs.class ?: ""} big-button ui-state-active"
//
//            out << "</div>"
//        }
        attrs.class += " btn btn-large"
        out << link(attrs) {
            if (attrs.value) {
                out << attrs.value
            } else {
                out << body()
            }

        }
    }

    def radioItem = { attrs ->
        if (session.isMobile) {
            out << radio(attrs)
        } else {
            out << radio(attrs)
        }

    }

    def dateField = { attrs, body ->
        if (attrs.value instanceof Date) {
            attrs.value = attrs.value.format("MM/dd/yyyy")
        }
        attrs.size = "10"
        attrs.class = "ui-corner-all datePicker"
        attrs.placeholder = "mm/dd/yyyy"
        out << textField(attrs, body)
    }

    def textField = { attrs, body ->
        if (session.isMobile) {
            out << """<li class="smallfield">
    <span class="narrow">
        <span class="name"><label for="${attrs.name ?: ''}">${message(code: attrs.code)}</label></span>
"""
            out << f.txtField(attrs)
            out << """</span>
</li>
"""

        } else {
            out << property(attrs) {
                out << f.txtField(attrs)
            }
        }

    }

    def bigTextField = { attrs, body ->
        def cssClass = "loginForm ${attrs.class ?: ''}"
        def code = attrs.code
        out << f.bigTextField(attrs, body)
//        if (session.isMobile) {
//            out << "<li class='bigfield'>"
//            def type = attrs.type ?: "text"
//            if (!attrs.otherAttrs) attrs.otherAttrs = [:]
//            attrs.otherAttrs.placeholder = attrs.placeholder ?: message(code: attrs.code) ?: ""
//            attrs.otherAttrs.value = attrs.value ?: ""
//            attrs.otherAttrs.name = attrs.name
//
//            out << f.txtField(attrs)
////            out << "<input placeholder='${attrs.placeholder}' name='${attrs.name}' type='${type}' value='${attrs.value ?: ""}' />"
//            out << "</li>"
//        } else {
//
//        }
    }

    def unitSelector = { attrs ->
        if (session.isMobile) {
            out << bigTextField(attrs)
        } else {
            out << f.dynamicUnitSelector(attrs)
        }
    }

    def optGroup = { attrs, body ->
        out << "<optgroup label='${attrs.label}'>"
        out << body();
        out << "</optgroup>"
    }

    def unitSelectorOptions = { attrs ->
        out << "<option value=''>${message(code: 'unitSelector.default')}</option>"
        ScoutUnitType.values().each { ScoutUnitType scoutUnitType ->
            out << optGroup(label: message(code: scoutUnitType.name() + ".altlabel")) {
                LeaderPositionType.values().findAll { it.scoutUnitTypes.contains(scoutUnitType) }?.each {
                    LeaderPositionType leaderPositionType ->
                        out << g.selectOption(value: leaderPositionType.name()) {
                            out << message(code: leaderPositionType.name() + ".label")
                        }
                }
            }
        }

        final Closure groupTypeFilter = { it != ScoutGroupType.Unit && it != ScoutGroupType.Group }
        ScoutGroupType.values().findAll(groupTypeFilter)?.each { ScoutGroupType scoutGroupType ->
            out << "<optgroup label='${message(code: scoutGroupType.name() + ".label")}'>"
            LeaderPositionType.values().findAll { it.scoutGroupTypes.contains(scoutGroupType) }?.each {
                LeaderPositionType leaderPositionType ->
                    out << "<option value='${leaderPositionType.name()}'>${message(code: leaderPositionType.name() + ".label")}</option>"
            }
            out << "</optgroup>"

        }
    }

    def certificationOptions = { attrs ->
        List<ProgramCertification> certifications = ProgramCertification.listOrderByPositionType()
        Map leaderPositionTypesToRequiredCertification = [:]
        certifications.each {
            ProgramCertification programCertification ->
                LeaderPositionType positionType = programCertification.positionType
                if (positionType != null) {
                    if (!leaderPositionTypesToRequiredCertification.containsKey(positionType)) {
                        leaderPositionTypesToRequiredCertification[positionType] = []
                    }
                    leaderPositionTypesToRequiredCertification[positionType] << programCertification.certification
                }
        }

        leaderPositionTypesToRequiredCertification.each { entry ->
            out << optGroup(label: message(code: entry.key.name() + ".label")) {
                entry.value.each {
                    Certification certification ->
                        out << g.selectOption(value: certification.id) {
                            out << certification.name
                        }
                }
            }
        }
    }

    def permission = { attrs ->
        Leader leader = attrs.leader
        Role role = attrs.role
        if (session.isMobile) {
//            out << pageItem { out << "Log in with a browser"}
        } else {
            out << checkBox(onclick: "togglePermission(this, ${leader?.id}, ${role?.id})", checked: leader.hasAuthority(role))
            out << message(code: "${role.authority}.label")
        }
    }

    def selecter = { attrs, body ->
        attrs.class = "selecter ${attrs.class ?: ''}"
        if (session.isMobile) {
            out << "<li class='select'>"
            if (attrs.from) {
                out << select(attrs)
            } else {
                out << selectWithBody(attrs, body)
            }

            out << "<span class='arrow'></span>"
            out << "</li>"
        } else {

            out << property(attrs) {
                if (attrs.from) {
                    out << select(attrs)
                } else {
                    out << selectWithBody(attrs, body)
                }
            }
        }
    }

    def bigSelecter = { attrs, body ->
//        if (session.isMobile) {
//            out << selecter(attrs, body)
//        } else {
        String id = attrs.remove("id")
        def label = message(code: attrs.code)

        attrs.placeholder = label
        out << selectWithBody(attrs, body)
//        }
    }

    def row = { attrs, body ->
        def rowType = attrs.class ?: (attrs.fluid ? "row-fluid" : "row")
        out << "<div class=\"$rowType\">"
        out << body()
        out << "</div>"
    }

    def rowFluid = { attrs, body ->
        out << "<div class=\"row-fluid\">"
        out << body()
        out << "</div>"
    }

    def column = { attrs, body ->
        out << "<div class=\"span${attrs.span ?: "6"} ${attrs.center ? "center" : ""}\">"
        if (attrs.center) out << "<div class='left-center'>"
        out << body()
        if (attrs.center) out << "</div>"
        out << "</div>"
    }

    def boxed = { attrs, body ->
        def span = attrs.span ?: "12"

        if (attrs.row) {
            out << "<div class=\"row\">"
        }

        out << """

        <div class="span$span ${attrs.class ?: ""} ${attrs.center ? "center" : ""}">
            <div class="content-box content-pad">
"""
        if (attrs.center) {
            out << "<div class='left-center'>"
        }

        out << body()

        if (attrs.center) {
            out << "</div>"
        }

        out << """
            </div>
        </div>
"""

        if (attrs.row) {
            out << "</div>"
        }
    }

    def section = { attrs, body ->
        if (attrs.row) {
            out << "<div class=\"row-fluid\">"
            attrs.span = "12"
            out << "<div class=\"span${attrs.span ?: "6"}\">"
        }

        out << "<div class=\"section\">"
        out << body()
        out << "</div>"

        if (attrs.row) {
            out << "</div>"
            out << "</div>"
        }
    }

    def ctxmenu = { attrs, body ->
        out << g.ctxmenu(attrs, body)
    }

    def ctxmenuItem = { attrs, body ->
        out << g.ctxmenuItem(attrs, body)
    }


}
