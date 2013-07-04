
    <ul class="nav primary">
        <g:each in="${menuItems}" var="menuItem">
            <g:mainMenuItem menuItem="${menuItem}">
                <g:menuItem controller="${params.controller}" action="${params.action}" menuItem="${menuItem}"/>
            </g:mainMenuItem>
        </g:each>

        <sec:ifLoggedIn>
            <li><a href="/scoutinghub/logout/"><g:message code="menu.logout"/></a></li>
        </sec:ifLoggedIn>

        <span style="float:right;">
            <sec:ifLoggedIn>
            %{--<div style="text-align:right">--}%
                <table width="100%">
                    <tr>
                        <td align="right">
                            <table>
                                <tr>
                                    <td align="right"><g:textField name="leaderQuery" id="leaderQuery" class="ui-corner-all"/></td>
                                    <td><a href="javascript:leaderQuery()" style="font-size:14px; padding:4px" class="ui-button ui-button-style">
                                        <g:message code="permission.index.searchButton"/></a></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>

            %{--</div>--}%
            </sec:ifLoggedIn>
        </span>

    </ul>

%{--<div class="clear"></div>--}%
<g:subMenu controller="${params.controller}" action="${params.action}"/>
%{--<div class="clear"></div>--}%
