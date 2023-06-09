package org.logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

public class CustomLogger {
    private Logger logger;
    private Marker parentMarker;
    private Marker jndiMarker;
    private boolean jndiCheckEnabled;

    /*
     * Reference: https://github.com/NCSC-NL/log4shell/blob/main/detection_mitigation/README.md#detection
     * This Regex also works on obfuscation using URL encoding. https://www.urlencoder.org/
     */
    private static final Pattern JNDI_PATTERN = Pattern.compile("(?im)(?:^|[\\n]).*?(?:[\\x24]|%(?:25%?)*24|\\\\u?0*(?:44|24))(?:[\\x7b]|%(?:25%?)*7b|\\\\u?0*(?:7b|173))[^\\n]*?((?:j|%(?:25%?)*(?:4a|6a)|\\\\u?0*(?:112|6a|4a|152))[^\\n]*?(?:n|%(?:25%?)*(?:4e|6e)|\\\\u?0*(?:4e|156|116|6e))[^\\n]*?(?:d|%(?:25%?)*(?:44|64)|\\\\u?0*(?:44|144|104|64))[^\\n]*?(?:[i\\x{130}\\x{131}]|%(?:25%?)*(?:49|69|C4%(?:25%?)*B0|C4%(?:25%?)*B1)|\\\\u?0*(?:111|69|49|151|130|460|131|461))[^\\n]*?(?:[\\x3a]|%(?:25%?)*3a|\\\\u?0*(?:72|3a))[^\\n]*?((?:l|%(?:25%?)*(?:4c|6c)|\\\\u?0*(?:154|114|6c|4c))[^\\n]*?(?:d|%(?:25%?)*(?:44|64)|\\\\u?0*(?:44|144|104|64))[^\\n]*?(?:a|%(?:25%?)*(?:41|61)|\\\\u?0*(?:101|61|41|141))[^\\n]*?(?:p|%(?:25%?)*(?:50|70)|\\\\u?0*(?:70|50|160|120))(?:[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163)))?|(?:r|%(?:25%?)*(?:52|72)|\\\\u?0*(?:122|72|52|162))[^\\n]*?(?:m|%(?:25%?)*(?:4d|6d)|\\\\u?0*(?:4d|155|115|6d))[^\\n]*?(?:[i\\x{130}\\x{131}]|%(?:25%?)*(?:49|69|C4%(?:25%?)*B0|C4%(?:25%?)*B1)|\\\\u?0*(?:111|69|49|151|130|460|131|461))|(?:d|%(?:25%?)*(?:44|64)|\\\\u?0*(?:44|144|104|64))[^\\n]*?(?:n|%(?:25%?)*(?:4e|6e)|\\\\u?0*(?:4e|156|116|6e))[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163))|(?:n|%(?:25%?)*(?:4e|6e)|\\\\u?0*(?:4e|156|116|6e))[^\\n]*?(?:[i\\x{130}\\x{131}]|%(?:25%?)*(?:49|69|C4%(?:25%?)*B0|C4%(?:25%?)*B1)|\\\\u?0*(?:111|69|49|151|130|460|131|461))[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163))|(?:[^\\n]*?(?:[i\\x{130}\\x{131}]|%(?:25%?)*(?:49|69|C4%(?:25%?)*B0|C4%(?:25%?)*B1)|\\\\u?0*(?:111|69|49|151|130|460|131|461))){2}[^\\n]*?(?:o|%(?:25%?)*(?:4f|6f)|\\\\u?0*(?:6f|4f|157|117))[^\\n]*?(?:p|%(?:25%?)*(?:50|70)|\\\\u?0*(?:70|50|160|120))|(?:c|%(?:25%?)*(?:43|63)|\\\\u?0*(?:143|103|63|43))[^\\n]*?(?:o|%(?:25%?)*(?:4f|6f)|\\\\u?0*(?:6f|4f|157|117))[^\\n]*?(?:r|%(?:25%?)*(?:52|72)|\\\\u?0*(?:122|72|52|162))[^\\n]*?(?:b|%(?:25%?)*(?:42|62)|\\\\u?0*(?:102|62|42|142))[^\\n]*?(?:a|%(?:25%?)*(?:41|61)|\\\\u?0*(?:101|61|41|141))|(?:n|%(?:25%?)*(?:4e|6e)|\\\\u?0*(?:4e|156|116|6e))[^\\n]*?(?:d|%(?:25%?)*(?:44|64)|\\\\u?0*(?:44|144|104|64))[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163))|(?:h|%(?:25%?)*(?:48|68)|\\\\u?0*(?:110|68|48|150))(?:[^\\n]*?(?:t|%(?:25%?)*(?:54|74)|\\\\u?0*(?:124|74|54|164))){2}[^\\n]*?(?:p|%(?:25%?)*(?:50|70)|\\\\u?0*(?:70|50|160|120))(?:[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163)))?)[^\\n]*?(?:[\\x3a]|%(?:25%?)*3a|\\\\u?0*(?:72|3a))|(?:b|%(?:25%?)*(?:42|62)|\\\\u?0*(?:102|62|42|142))[^\\n]*?(?:a|%(?:25%?)*(?:41|61)|\\\\u?0*(?:101|61|41|141))[^\\n]*?(?:[s\\x{17f}]|%(?:25%?)*(?:53|73|C5%(?:25%?)*BF)|\\\\u?0*(?:17f|123|577|73|53|163))[^\\n]*?(?:e|%(?:25%?)*(?:45|65)|\\\\u?0*(?:45|145|105|65))[^\\n]*?(?:[\\x3a]|%(?:25%?)*3a|\\\\u?0*(?:72|3a))(JH[s-v]|[\\x2b\\x2f-9A-Za-z][CSiy]R7|[\\x2b\\x2f-9A-Za-z]{2}[048AEIMQUYcgkosw]ke[\\x2b\\x2f-9w-z]))");

    public CustomLogger(Marker parentMarker, boolean checkJndi) {
        logger = LogManager.getLogger(String.format("CustomLogger-%s", parentMarker.getName()));
        this.parentMarker = parentMarker;
        jndiMarker = MarkerManager.getMarker("JNDI").setParents(parentMarker);
        this.jndiCheckEnabled = checkJndi;
    }

    public void debug(String message) {
        safeFormatAndLog(logger::debug, message);
    }

    public void info(String message) {
        safeFormatAndLog(logger::info, message);
    }

    public void trace(String message) {
        safeFormatAndLog(logger::trace, message);
    }

    public void warn(String message) {
        safeFormatAndLog(logger::warn, message);
    }

    public void error(String message) {
        safeFormatAndLog(logger::error, message);
    }

    private void safeFormatAndLog(BiConsumer<Marker, String> log, String message) {
        Optional.ofNullable(message)
            .filter(not(String::isBlank))
            .ifPresent(m -> {
                if (jndiCheckEnabled && JNDI_PATTERN.matcher(m).find()) {
                    log.accept(jndiMarker, URLEncoder.encode(message, StandardCharsets.UTF_8));
                } else {
                    log.accept(parentMarker, m);
                }
            });
    }
}