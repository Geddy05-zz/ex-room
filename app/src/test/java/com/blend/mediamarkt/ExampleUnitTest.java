package com.blend.mediamarkt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        int a = ApiHandler.calculate(2,2);
        assertEquals(4, a);
    }
}