package view;

import java.util.List;

/**
 * Created by Frederik on 01/12/2016.
 */
public class HTMLGenerator {
    public static String generate(List<GameMessage> messages) {
        // Header
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
                "\tmargin: 40px;\n" +
                "  display: inline-block;\n" +
                "  position: relative;\n" +
                "\twidth: 86%;\n" +
                "\theight: auto;\n" +
                "\tbackground-color: lightyellow;\n" +
                "}\n" +
                ".border{\n" +
                "  border: 8px solid #666;\n" +
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
                "\tborder-color: #666 transparent transparent transparent;\n" +
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
                "\tborder-color: lightyellow transparent transparent transparent;\n" +
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
                "\tborder-color: #666 #666 transparent transparent;\n" +
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
                "\tborder-color: lightyellow lightyellow transparent transparent;\n" +
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
                "\tborder-color: transparent transparent transparent #666;\n" +
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
                "\tborder-color: transparent transparent transparent lightyellow;\n" +
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
                "\tborder-color: #666 transparent transparent #666;\n" +
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
                "\tborder-color: lightyellow transparent transparent lightyellow;\n" +
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
                "\tborder-color: #666 #666 transparent transparent;\n" +
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
                "\tborder-color: lightyellow lightyellow transparent transparent;\n" +
                "}\n" +
                "/*\n" +
                "\tleft: -8px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -40px;\n" +
                "\tborder: 32px solid;\n" +
                "\tborder-color: transparent transparent transparent #666;\n" +
                "\tleft: 0px;\n" +
                "  right: auto;\n" +
                "  top: auto;\n" +
                "\tbottom: -20px;\n" +
                "\tborder: 22px solid;\n" +
                "\tborder-color: transparent transparent transparent lightyellow;\n" +
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
                "\tborder-color: #666 #666 transparent transparent;\n" +
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
                "\tborder-color: lightyellow lightyellow transparent transparent;\n" +
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
                "\tborder-color: #666 transparent transparent #666;\n" +
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
                "\tborder-color: lightyellow transparent transparent lightyellow;\n" +
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
                "\tborder-color: #666 transparent transparent transparent;\n" +
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
                "\tborder-color: lightyellow transparent transparent transparent;\n" +
                "}\n" +
                "\n" +
                "/* talk bubble contents */\n" +
                ".talktext{\n" +
                "  padding: 1em;\n" +
                "\ttext-align: left;\n" +
                "  line-height: 1.5em;\n" +
                "}\n" +
                ".talktext p{\n" +
                "  /* remove webkit p margins */\n" +
                "  -webkit-margin-before: 0em;\n" +
                "  -webkit-margin-after: 0em;\n" +
                "}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body onload='toBottom()'>\n";

        for (GameMessage message : messages) {
            if (message.isMessageFromGame()) {
                res = res + "<div class=\"talk-bubble tri-right round border left-top\">\n";
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
