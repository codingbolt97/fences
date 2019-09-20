package com.philips.bootcamp.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.google.gson.JsonObject;
import com.philips.bootcamp.domain.Constants;
import com.philips.bootcamp.utils.FileUtils;

import org.junit.Test;
import org.mockito.Mockito;

public class CheckstyleTest {

    @Test
    public void getNameReturnsCheckstyle() {
        Checkstyle checkstyle = new Checkstyle();
        assertEquals("checkstyle", checkstyle.getName());
    }

    @Test
    public void getDescriptionReturnsContensOfCheckstyle_dot_desc() {
        File checkstyleDesc = new File(Constants.toolsDirectory, "checkstyle.desc");
        String checkstyleDescContents = FileUtils.getFileContents(checkstyleDesc);
        Checkstyle checkstyle = new Checkstyle();
        assertEquals(checkstyleDescContents, checkstyle.getDescription());
    }

    @Test
    public void defaultSettingsReturnsDefaults() {
        Checkstyle checkstyle = new Checkstyle();
        JsonObject actualDefault = checkstyle.getDefaultSettings();
        
        JsonObject expectedDefault = new JsonObject();
        expectedDefault.addProperty("styleguide", "google_checks");
        expectedDefault.addProperty("excludeTestFiles", "no");

        assertEquals(expectedDefault, actualDefault);
    }

    @Test
    public void verifySettingsReturnsFalseForNullArg() {
        Checkstyle checkstyle = new Checkstyle();
        assertTrue(!checkstyle.verifySettings(null));
    }

    @Test
    public void verifySettingsIsPassedRandomSettingsAndItReturnsFalse() {
        Checkstyle checkstyle = new Checkstyle();
        JsonObject settings = new JsonObject();
        settings.addProperty("styleguide", "nonsense");
        assertTrue(!checkstyle.verifySettings(settings));

        settings = new JsonObject();
        settings.addProperty("excludeTestFiles", "nonsense");
        assertTrue(!checkstyle.verifySettings(settings));
    }

    @Test
    public void verifySettingsIsPassedProperSettingsAndItReturnsTrue() {
        Checkstyle checkstyle = new Checkstyle();
        JsonObject settings = new JsonObject();
        settings.addProperty("styleguide", "google_checks");
        assertTrue(checkstyle.verifySettings(settings));

        settings.addProperty("styleguide", "sun_checks");
        assertTrue(checkstyle.verifySettings(settings));

        settings = new JsonObject();
        settings.addProperty("excludeTestFiles", "no");
        assertTrue(checkstyle.verifySettings(settings));

        settings.addProperty("excludeTestFiles", "yes");
        assertTrue(checkstyle.verifySettings(settings));
    }

    @Test
    public void execute() {
        Checkstyle cs = Mockito.mock(Checkstyle.class);
        JsonObject settings = new JsonObject();
        settings.addProperty("styleguide", "sun_checks");
        settings.addProperty("excludeTestFiles", "no");
        settings.addProperty("project", "test");
        JsonObject returnValue = new JsonObject();
        //Mockito.when(TerminalUtils.run("java -jar ./../tools/checkstyle-8.23-all.jar -c sun_checks.xml \"test\" -f xml -e target"))
        //    .thenReturn("value");
        Mockito.when(cs.parseXml("value")).thenReturn(returnValue);
        Mockito.when(cs.execute(settings)).thenCallRealMethod();

        JsonObject result = cs.execute(settings);
        assertEquals(returnValue, result);
    }

    @Test
    public void parseXml() {
        Checkstyle cs = new Checkstyle();
        String out = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<checkstyle version=\"8.23\">" +
        "<file name=\"C:\\Program Files\\Apache Tomcat 8\\bin\\.\\..\\sources\\test\\Main.java\">" +
        "<error line=\"1\" severity=\"error\" message=\"File does not end with a newline.\"/>" +
        "</file>" +
        "</checkstyle>";
        JsonObject result = cs.parseXml(out);
        String expectedJson = "{\"C:\\\\Program Files\\\\Apache Tomcat 8\\\\bin\\\\.\\\\..\\\\sources\\\\test\\\\Main.java\":[{\"line\":\"1\",\"severity\":\"error\",\"message\":\"File does not end with a newline.\"}]}";
        assertEquals(expectedJson, result.toString());
    }

    @Test
    public void nullForNull() {
        Checkstyle cs = new Checkstyle();
        assertNull(cs.parseXml(null));
    }
}