package view;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.List;

/**
 * Renders the html code for the gui to show the message history<br>
 *     Style is inspired by <a href="https://codepen.io/Founts/pen/gmhcl">Jason Founts</a>
 */
class HTMLGenerator {
    /**
     * Renders the html code for the gui to show the message history
     *
     * @param messages The List of {@link GameMessage}s to render.
     * @return The html code ready to be shown in any kind of web browser
     */
    static String generate(List<GameMessage> messages) {
        // Header

        String borderColor = "#000000";
        String bubbleBackground = "white";

        // TODO: give messages from player and from game a different background color

        String res = "<html>\n" +
                "<head>\n" +
                "<script language=\"javascript\" type=\"text/javascript\">  \n" +
                "        function toBottom(){\n" +
                "        window.scrollTo(0, document.body.scrollHeight)  \n" +
                "        } \n" +
                "    </script>" +
                "    <style>\n" +
                "        /* General CSS Setup */\n" +
                "body{\n" +
                "  background-color: lightblue;\n" +
                "  font-family: \"Ubuntu-Italic\", \"Lucida Sans\", helvetica, sans;\n" +
                "}\n" +
                "\n" +
                "/* container */\n" +
                ".container {\n" +
                "  padding: 5% 5%;\n" +
                "}\n" +
                "\n" +
                "/* CSS talk bubble */\n" +
                ".talk-bubble {\n" +
                "\tmargin: 10px;\n" +
                "\tdisplay: inline-block;\n" +
                "\ttransform: translate(+7%);\n" +
                "\tposition: relative;\n" +
                "\twidth: 86%;\n" +
                "\tword-wrap: break-word;\n" +
                "\theight: auto;\n" +
                "\tbackground-color: " + bubbleBackground + ";\n" +
                "}\n" +
                ".border{\n" +
                "  border: 8px solid " + borderColor + ";\n" +
                "}\n" +
                ".round{\n" +
                "  border-radius: 30px;\n" +
                "\t-webkit-border-radius: 30px;\n" +
                "\t-moz-border-radius: 30px;\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/* Right triangle placed top left flush. */\n" +
                ".tri-right.border.left-top:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: -40px;\n" +
                "\tright: auto;\n" +
                "  top: -8px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 32px solid;\n" +
                "\tborder-color: " + borderColor + " transparent transparent transparent;\n" +
                "}\n" +
                ".tri-right.left-top:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: -20px;\n" +
                "\tright: auto;\n" +
                "  top: 0px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 22px solid;\n" +
                "\tborder-color: " + bubbleBackground + " transparent transparent transparent;\n" +
                "}\n" +
                "\n" +
                "/* Right triangle, left side slightly down */\n" +
                ".tri-right.border.left-in:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: -40px;\n" +
                "\tright: auto;\n" +
                "  top: 30px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + borderColor + " " + borderColor + " transparent transparent;\n" +
                "}\n" +
                ".tri-right.left-in:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: -20px;\n" +
                "\tright: auto;\n" +
                "  top: 38px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 12px solid;\n" +
                "\tborder-color: " + bubbleBackground + " " + bubbleBackground + " transparent transparent;\n" +
                "}\n" +
                "\n" +
                "/*Right triangle, placed bottom left side slightly in*/\n" +
                ".tri-right.border.btm-left:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "\tleft: -8px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 32px solid;\n" +
                "\tborder-color: transparent transparent transparent " + borderColor + ";\n" +
                "}\n" +
                ".tri-right.btm-left:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "\tleft: 0px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 22px solid;\n" +
                "\tborder-color: transparent transparent transparent " + bubbleBackground + ";\n" +
                "}\n" +
                "\n" +
                "/*Right triangle, placed bottom left side slightly in*/\n" +
                ".tri-right.border.btm-left-in:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "\tleft: 30px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + borderColor + " transparent transparent " + borderColor + ";\n" +
                "}\n" +
                ".tri-right.btm-left-in:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "\tleft: 38px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 12px solid;\n" +
                "\tborder-color: " + bubbleBackground + " transparent transparent " + bubbleBackground + ";\n" +
                "}\n" +
                "\n" +
                "/*Right triangle, placed bottom right side slightly in*/\n" +
                ".tri-right.border.btm-right-in:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: 30px;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + borderColor + " " + borderColor + " transparent transparent;\n" +
                "}\n" +
                ".tri-right.btm-right-in:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: 38px;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 12px solid;\n" +
                "\tborder-color: " + bubbleBackground + " " + bubbleBackground + " transparent transparent;\n" +
                "}\n" +
                "/*\n" +
                "\tleft: -8px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 32px solid;\n" +
                "\tborder-color: transparent transparent transparent " + borderColor + ";\n" +
                "\tleft: 0px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 22px solid;\n" +
                "\tborder-color: transparent transparent transparent " + bubbleBackground + ";\n" +
                "\n" +
                "/*Right triangle, placed bottom right side slightly in*/\n" +
                ".tri-right.border.btm-right:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: -8px;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + borderColor + " " + borderColor + " transparent transparent;\n" +
                "}\n" +
                ".tri-right.btm-right:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: 0px;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 12px solid;\n" +
                "\tborder-color: " + bubbleBackground + " " + bubbleBackground + " transparent transparent;\n" +
                "}\n" +
                "\n" +
                "/* Right triangle, right side slightly down*/\n" +
                ".tri-right.border.right-in:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: -40px;\n" +
                "  top: 30px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + borderColor + " transparent transparent " + borderColor + ";\n" +
                "}\n" +
                ".tri-right.right-in:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: -20px;\n" +
                "  top: 38px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 12px solid;\n" +
                "\tborder-color: " + bubbleBackground + " transparent transparent " + bubbleBackground + ";\n" +
                "}\n" +
                "\n" +
                "/* Right triangle placed top right flush. */\n" +
                ".tri-right.border.right-top:before {\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: -40px;\n" +
                "  top: -8px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 32px solid;\n" +
                "\tborder-color: " + borderColor + " transparent transparent transparent;\n" +
                "}\n" +
                ".tri-right.right-top:after{\n" +
                "\tcontent: ' ';\n" +
                "\tposition: absolute;\n" +
                "\twidth: 0;\n" +
                "\theight: 0;\n" +
                "  left: auto;\n" +
                "\tright: -20px;\n" +
                "  top: 0px;\n" +
                "\tbottom: auto;\n" +
                "\tborder: 20px solid;\n" +
                "\tborder-color: " + bubbleBackground + " transparent transparent transparent;\n" +
                "}\n" +
                "\n" +
                "/* talk bubble contents */\n" +
                ".talktext{\n" +
                "\tpadding-left: 1em;\n" +
                "\tpadding-right: 1em;" +
                "\tpadding-top: 0.1em;" +
                "\tpadding-bottom: 0.1em;" +
                "\ttext-align: left;\n" +
                "\tline-height: 1.5em;\n" +
                "}\n" +
                ".talktext p{\n" +
                "\t/* remove webkit p margins */\n" +
                "\t-webkit-margin-before: 0em;\n" +
                "\t-webkit-margin-after: 0em;\n" +
                "}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body onload='toBottom()'>\n";

        for (GameMessage message : messages) {
            if (message.isMessageFromGame()) {
                res = res + "<div class=\"talk-bubble tri-right border left-top\">\n";
            } else {
                res = res + "<div class=\"talk-bubble tri-right round border right-top\">\n";
            }

            res = res + "<div class=\"talktext\">\n" +
                    "        <p>" + message.getMessage().replace("\n", "<br>") + "</p>\n" +
                    "    </div>\n" +
                    "</div>\n";
        }

        res = res + "</body>\n" +
                "</html>\n";

        return res;
    }
}
