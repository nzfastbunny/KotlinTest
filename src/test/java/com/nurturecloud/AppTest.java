package com.nurturecloud;

import com.nurturecloud.model.Suburb;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class AppTest {
    private PrintStream sysOut;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        sysOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void revertStreams() {
        System.setOut(sysOut);
    }

    @Test
    public void testProcessSuburbs_invalidSuburb() {
        String input = "Atlantis\n2000\n";
        App.processSuburbs(new HashMap<>(), new HashMap<>(), new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Incorrect suburb and postcode combination - ATLANTIS and 2000\n" +
                "Please check the details and try again"));
    }

    @Test
    public void testProcessSuburbs_invalidPostcode() {
        String input = "Test\n232323\n";
        App.processSuburbs(new HashMap<>(), new HashMap<>(), new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Incorrect suburb and postcode combination - TEST and 232323\n" +
                "Please check the details and try again"));
    }

    @Test
    public void testProcessSuburbs_invalidCombo() {
        String input = "Rosebery\n2000\n";
        App.processSuburbs(new HashMap<>(), new HashMap<>(), new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Incorrect suburb and postcode combination - ROSEBERY and 2000\n" +
                "Please check the details and try again"));
    }

    @Test
    public void testProcessSuburbs_noEntries() {
        String input = "\n\n";
        App.processSuburbs(new HashMap<>(), new HashMap<>(), new Scanner(input));
        String output = outContent.toString();
        Assert.assertEquals("Please enter a suburb name: Please enter the postcode: ", output);
    }

    @Test
    public void testProcessSuburbs_nonPhysicalAddress() {
        String input = "chatswood\n2057\n"; // also tests case insensitivity
        HashMap<String, Suburb> suburbMap = new HashMap<>();
        Suburb suburb = new Suburb(2057, "CHATSWOOD", "NSW", null, null);
        suburbMap.put("CHATSWOOD-2057", suburb);

        App.processSuburbs(suburbMap, new HashMap<>(), new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("The supplied suburb and postcode combination (CHATSWOOD, 2057) is a non-physical address\n" +
                "Please check the details and try again"));
    }

    @Test
    public void testProcessSuburbs() {
        String input = "Sydney\n2000\n"; // also tests case insensitivity
        HashMap<String, Suburb> suburbMap = new HashMap<>();
        HashMap<String, List<Suburb>> stateMap = new HashMap<>();
        stateMap.put("NSW", new ArrayList<>());

        Suburb suburb = new Suburb(2000, "SYDNEY", "NSW",
                new BigDecimal(151.2099), new BigDecimal(-33.8697));
        suburbMap.put("SYDNEY-2000", suburb);
        stateMap.get("NSW").add(suburb);

        suburb = new Suburb(2010, "SURRY HILLS", "NSW",
                new BigDecimal(151.21), new BigDecimal(-33.8849));
        suburbMap.put("SURRY HILLS-2010", suburb);
        stateMap.get("NSW").add(suburb);

        suburb = new Suburb(2136, "BURWOOD HEIGHTS", "NSW",
                new BigDecimal(151.1039), new BigDecimal(-33.8893));
        suburbMap.put("BURWOOD HEIGHTS-2136", suburb);
        stateMap.get("NSW").add(suburb);

        App.processSuburbs(suburbMap, stateMap, new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Nearby Suburbs:\r\n\tSURRY HILLS  2010"));
        Assert.assertTrue(output.contains("Fringe Suburbs:\r\n\tBURWOOD HEIGHTS  2136"));
    }

    @Test
    public void testProcessSuburbs_noneFound() {
        String input = "Sydney\n2000\n"; // also tests case insensitivity
        HashMap<String, Suburb> suburbMap = new HashMap<>();
        HashMap<String, List<Suburb>> stateMap = new HashMap<>();
        stateMap.put("NSW", new ArrayList<>());

        Suburb suburb = new Suburb(2000, "SYDNEY", "NSW",
                new BigDecimal(151.2099), new BigDecimal(-33.8697));
        suburbMap.put("SYDNEY-2000", suburb);
        stateMap.get("NSW").add(suburb);

        App.processSuburbs(suburbMap, stateMap, new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Nothing found for SYDNEY, 2000!!"));
    }

    @Test
    public void testProcessSuburbs_2close() {
        String input = "Sydney\n2000\n"; // also tests case insensitivity
        HashMap<String, Suburb> suburbMap = new HashMap<>();
        HashMap<String, List<Suburb>> stateMap = new HashMap<>();
        stateMap.put("NSW", new ArrayList<>());

        Suburb suburb = new Suburb(2000, "SYDNEY", "NSW",
                new BigDecimal(151.2099), new BigDecimal(-33.8697));
        suburbMap.put("SYDNEY-2000", suburb);
        stateMap.get("NSW").add(suburb);

        suburb = new Suburb(2203, "DULWICH HILL", "NSW",
                new BigDecimal(151.1382), new BigDecimal(-33.9046));
        suburbMap.put("DULWICH HILL-2203", suburb);
        stateMap.get("NSW").add(suburb);

        suburb = new Suburb(2010, "SURRY HILLS", "NSW",
                new BigDecimal(151.21), new BigDecimal(-33.8849));
        suburbMap.put("SURRY HILLS-2010", suburb);
        stateMap.get("NSW").add(suburb);

        App.processSuburbs(suburbMap, stateMap, new Scanner(input));
        String output = outContent.toString();
        Assert.assertTrue(output.contains("Nearby Suburbs:\r\n\tSURRY HILLS  2010\r\n\tDULWICH HILL  2203"));
    }

    @Test
    public void testFindDistance_nulls() {
        BigDecimal result = App.findDistance(new Suburb(), new Suburb());

        BigDecimal expected = new BigDecimal(100);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testFindDistance() {
        Suburb home = new Suburb(2000, "SYDNEY", "NSW",
                new BigDecimal(151.2099), new BigDecimal(-33.8697));
        Suburb nearby = new Suburb(2010, "SURRY HILLS", "NSW",
                new BigDecimal(151.21), new BigDecimal(-33.8849));

        BigDecimal result = App.findDistance(home, nearby);

        BigDecimal expected = new BigDecimal(1.6890).setScale(2, RoundingMode.HALF_EVEN);
        Assert.assertEquals(expected, result);

        home = new Suburb(2018, "ROSEBERY", "NSW",
                new BigDecimal(151.2048), new BigDecimal(-33.9186));
        nearby = new Suburb(2060, "WAVERTON", "NSW",
                new BigDecimal(151.1988), new BigDecimal(-33.8381));

        result = App.findDistance(home, nearby);

        expected = new BigDecimal(8.9724469).setScale(2, RoundingMode.HALF_EVEN);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testDegreesToRadians() {
        // A quick test to see if it lines up with the 5 results from an external trusted source
        BigDecimal degrees = new BigDecimal(50);
        BigDecimal radians = App.degreesToRadians(degrees);

        BigDecimal rightAnswer = new BigDecimal(0.872665).setScale(6, RoundingMode.HALF_EVEN);
        Assert.assertEquals(rightAnswer, radians.setScale(6, RoundingMode.HALF_EVEN));

        degrees = new BigDecimal(15);
        radians = App.degreesToRadians(degrees);

        rightAnswer = new BigDecimal(0.261799).setScale(6, RoundingMode.HALF_EVEN);
        Assert.assertEquals(rightAnswer, radians.setScale(6, RoundingMode.HALF_EVEN));

        degrees = new BigDecimal(5.678);
        radians = App.degreesToRadians(degrees);

        rightAnswer = new BigDecimal(0.099099795).setScale(6, RoundingMode.HALF_EVEN);
        Assert.assertEquals(rightAnswer, radians.setScale(6, RoundingMode.HALF_EVEN));

        degrees = new BigDecimal(8.347);
        radians = App.degreesToRadians(degrees);

        rightAnswer = new BigDecimal(0.14568263).setScale(6, RoundingMode.HALF_EVEN);
        Assert.assertEquals(rightAnswer, radians.setScale(6, RoundingMode.HALF_EVEN));

        degrees = new BigDecimal(22.765);
        radians = App.degreesToRadians(degrees);

        rightAnswer = new BigDecimal(0.397324204).setScale(6, RoundingMode.HALF_EVEN);
        Assert.assertEquals(rightAnswer, radians.setScale(6, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testLoadSuburbsSuccessfully() {
        List<Suburb> suburbs = App.loadSuburbs();

        // Check all the suburbs have been loaded
        Assert.assertFalse(suburbs.isEmpty());
        Assert.assertEquals(16544, suburbs.size()); // the current number of suburbs in the JSON file

        // Check a couple of the objects to see if they have been populated correctly
        Suburb firstOne = suburbs.get(0); // no long or lat though
        Assert.assertTrue(200 == firstOne.getPcode());
        Assert.assertEquals("AUSTRALIAN NATIONAL UNIVERSITY", firstOne.getLocality());
        Assert.assertEquals("ACT", firstOne.getState());
        Assert.assertNull(firstOne.getLatitude());
        Assert.assertNull(firstOne.getLongitude());

        Suburb secondOne = suburbs.get(1); // has long and lat
        Assert.assertTrue(800 == secondOne.getPcode());
        Assert.assertEquals("DARWIN", secondOne.getLocality());
        Assert.assertEquals("NT", secondOne.getState());
        Assert.assertEquals(new BigDecimal(-12.4633).setScale(4, RoundingMode.HALF_EVEN), secondOne.getLatitude());
        Assert.assertEquals(new BigDecimal(130.8434).setScale(4, RoundingMode.HALF_EVEN), secondOne.getLongitude());
    }
}
