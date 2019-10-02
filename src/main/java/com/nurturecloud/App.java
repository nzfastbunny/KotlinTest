package com.nurturecloud;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nurturecloud.model.DistanceComparator;
import com.nurturecloud.model.Result;
import com.nurturecloud.model.Suburb;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * An application to find the nearby (< 10km) and fringe (< 50km) suburbs to the suburb supplied.
 */
public class App {
    private final static Logger LOG = Logger.getLogger(App.class.getName());
    /**
     * The mean radius of the Earth in kms
     */
    private static final int EARTH_MEAN_RADIUS = 6371;

    public static void main(String[] args) {
        List<Suburb> suburbs = loadSuburbs(); // load the suburb object from the provided JSON

        // SuburbMap is needed to link the supplied info to a suburb object
        Map<String, Suburb> suburbMap = new HashMap<>();
        // StateMap is used to focus the searching on the likely closest suburb (not as effective on boarder towns though)
        Map<String, List<Suburb>> stateMap = new HashMap<>();
        for (Suburb suburb : suburbs) {
            suburbMap.put(suburb.getLocality() + "-" + suburb.getPcode(), suburb);

            String state = suburb.getState();
            if (!stateMap.containsKey(state)) {
                stateMap.put(state, new ArrayList<>());
            }
            stateMap.get(state).add(suburb);
        }

        // Accept the command line input and process the results
        Scanner command = new Scanner(System.in);
        processSuburbs(suburbMap, stateMap, command);
        command.close();
    }

    /**
     * Using the command line input, process the suburbs to find the best matches for nearby and fringe suburbs
     *
     * @param suburbMap the map of the all the suburbs available keyed by the suburb-postcode concatenation
     * @param stateMap  the map of list of suburbs keyed by their state/territory
     * @param command   the command line input scanner (done this way to make unit tests easier/possible)
     */
    static void processSuburbs(Map<String, Suburb> suburbMap, Map<String, List<Suburb>> stateMap, Scanner command) {
        boolean running = true;
        while (running) {
            // Ask for user input (added checking included for unit testing)
            System.out.print("Please enter a suburb name: ");
            String suburbName = "";
            if (command.hasNext()) {
                suburbName = command.nextLine().toUpperCase();
            }

            System.out.print("Please enter the postcode: ");
            String postcode = "";
            if (command.hasNext()) {
                postcode = command.nextLine().toUpperCase();
            }

            // Mostly included for unit testing but also a way to exit the application gracefully
            if (suburbName.isEmpty() && postcode.isEmpty()) {
                running = false;
                continue; // Effectively end the application
            }

            Suburb home = suburbMap.get(suburbName + "-" + postcode);

            // Check for incorrect details
            if (home == null) {
                System.out.println(String.format("Incorrect suburb and postcode combination - %s and %s\n" +
                        "Please check the details and try again", suburbName, postcode));
                continue;
            }

            // Check for non-physical address (no location details - therefore no distance can be determined)
            if (home.getLatitude() == null || home.getLongitude() == null) {
                System.out.println(String.format("The supplied suburb and postcode combination (%s, %s) is a non-physical address\n" +
                        "Please check the details and try again", suburbName, postcode));
                continue;
            }

            // Get the list of suburbs for the state of the supplied suburb
            List<Suburb> localSuburbs = stateMap.get(home.getState());

            // Find the closest suburbs
            Map<String, List<Result>> results = findCloseSuburbs(home, localSuburbs);

            // Output the findings
            outputResults(suburbName, postcode, results);
        }
    }

    /**
     * Find the nearby and fringe suburbs given the supplied suburb and the list of suburbs in a similar area
     *
     * @param home         the supplied suburb
     * @param localSuburbs the list of suburbs from the supplied suburb's state
     * @return the map of results containing 2 list of nearby and fringe suburbs
     */
    static Map<String, List<Result>> findCloseSuburbs(Suburb home, List<Suburb> localSuburbs) {
        Map<String, List<Result>> results = new HashMap<>(2);
        results.put("Nearby", new ArrayList<Result>());
        results.put("Fringe", new ArrayList<Result>());

        for (Suburb suburb : localSuburbs) {
            BigDecimal distance = findDistance(home, suburb);

            List<Result> nearbyList = results.get("Nearby");
            List<Result> fringeList = results.get("Fringe");

            // If the distance is between 0 and 10kms then it is a nearby suburb (add it to the list
            if (distance.compareTo(BigDecimal.ZERO) > 0 && distance.compareTo(BigDecimal.TEN) <= 0) {
                Result nearby = new Result(suburb.getLocality(), suburb.getPcode(), distance);
                nearbyList.add(nearby);
            } else if (distance.compareTo(new BigDecimal(10)) > 0 && // Fringe suburbs - 10 to 50kms away
                    distance.compareTo(new BigDecimal(50)) <= 0) {
                Result fringe = new Result(suburb.getLocality(), suburb.getPcode(), distance);
                fringeList.add(fringe);
            }

            // Collecting 600 results each to choose from seemed the best balance of time and accuracy
            if (nearbyList.size() > 600 && fringeList.size() > 600) {
                return results;
            }
        }

        return results;
    }

    /**
     * Output the results of the search to the user - nearby and fringe lists of suburbs
     *
     * @param suburbName the name of the suburb supplied
     * @param postcode   the post code of the suburb supplied
     * @param results    the map of results - containing nearby and fringe lists of suburbs
     */
    static void outputResults(String suburbName, String postcode, Map<String, List<Result>> results) {
        List<Result> nearbyList = results.get("Nearby");
        List<Result> fringeList = results.get("Fringe");

        if (nearbyList.isEmpty() && fringeList.isEmpty()) {
            System.out.println(String.format("Nothing found for %s, %s!!\n", suburbName, postcode));
        } else {
            // Sort the results to list the closest suburbs first
            Collections.sort(nearbyList, new DistanceComparator());
            Collections.sort(fringeList, new DistanceComparator());

            // Clear away the unnecessary results to get a maximum of 15
            if (nearbyList.size() > 15) {
                nearbyList.subList(15, nearbyList.size()).clear();
            }

            if (fringeList.size() > 15) {
                fringeList.subList(15, fringeList.size()).clear();
            }

            // Print out the result to the user
            System.out.println("\nNearby Suburbs:");
            nearbyList.forEach(suburb -> System.out.println("\t" + suburb.getSuburb() + "  " + suburb.getPostCode()));

            System.out.println("\nFringe Suburbs:");
            fringeList.forEach(suburb -> System.out.println("\t" + suburb.getSuburb() + "  " + suburb.getPostCode()));
            System.out.print("\n\n");
        }
    }

    /**
     * Use the Haversine formula to determine the distance between 2 points on the Earth
     *
     * @param home   the supplied suburb
     * @param suburb one of the possible nearby (or fringe) suburbs
     * @return the distance in kilometres (kms) between the 2 suburbs
     */
    static BigDecimal findDistance(Suburb home, Suburb suburb) {
        /**
         * Formula for distance is:
         *         a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
         *         c = 2 ⋅ atan2( √a, √(1−a) )
         *         d = R ⋅ c
         *         where	φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
         *         note that angles need to be in radians to pass to trig functions!
         */
        BigDecimal lat1 = home.getLatitude();
        BigDecimal lon1 = home.getLongitude();

        BigDecimal lat2 = suburb.getLatitude();
        BigDecimal lon2 = suburb.getLongitude();

        // If we don't have all the coordinates then we can do the calculation
        if (lat1 == null || lat2 == null || lon1 == null || lon2 == null) {
            return new BigDecimal(100); // return a value that will be filtered out
        }

        BigDecimal earthRadiusKm = new BigDecimal(EARTH_MEAN_RADIUS);

        BigDecimal dLat = lat2.subtract(lat1);
        BigDecimal dLon = lon2.subtract(lon1);
        dLat = degreesToRadians(dLat);
        dLon = degreesToRadians(dLon);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        BigDecimal a = new BigDecimal(Math.sin(dLat.doubleValue() / 2) * Math.sin(dLat.doubleValue() / 2) +
                Math.sin(dLon.doubleValue() / 2) * Math.sin(dLon.doubleValue() / 2)
                        * Math.cos(lat1.doubleValue()) * Math.cos(lat2.doubleValue()));
        BigDecimal c = new BigDecimal(2 * Math.atan2(Math.sqrt(a.doubleValue()), Math.sqrt(1 - a.doubleValue())));
        return earthRadiusKm.multiply(c).setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert the degrees to radians to use in the calculations
     *
     * @param degrees the coordinate in degrees
     * @return the coordinate in radians
     */
    static BigDecimal degreesToRadians(BigDecimal degrees) {
        return degrees.multiply(new BigDecimal(Math.PI / 180));
    }

    /**
     * Load the suburbs from the supplied JSON file
     *
     * @return the list of suburbs
     */
    static List<Suburb> loadSuburbs() {
        try {
            Gson gson = new Gson();
            URL url = Resources.getResource("aus_suburbs.json");
            String result = Resources.toString(url, Charsets.UTF_8);

            // Convert JSON string to a list of Suburb objects
            return gson.fromJson(result, new TypeToken<List<Suburb>>() {}.getType());
        } catch (IOException e) {
            LOG.warning("An error has occurred while loading the suburbs");
            return new ArrayList<>();
        }
    }
}
