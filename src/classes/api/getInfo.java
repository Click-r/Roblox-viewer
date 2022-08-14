package classes.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.concurrent.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

import java.net.*;

import java.time.*;

import classes.UserNotFoundException;
import classes.Link;
import classes.Player;

import loaders.AdvancedSettings;
import loaders.SearchSettings;

import ui.gui.err.ErrorHandler;

public class getInfo {

    final public static int numData = classes.Player.class.getFields().length;
    final public static int numEndpoints = 5;

    @SuppressWarnings("static-access")

    private static String dateLocalTime(String time, String... prefered) {
        TimeZone timezone = Calendar.getInstance().getTimeZone();

        String abbrev = timezone.getDisplayName(false, TimeZone.SHORT);
        ZoneId zId = timezone.toZoneId();
        
        timezone.setDefault(TimeZone.getTimeZone(zId)); // set default time zone to local
        
        if (prefered.length == 1) {
            String prefTimezone = prefered[0].toUpperCase();
            TimeZone chosen = timezone.getTimeZone(prefTimezone); // resorts to the default set earlier if it can't find it
            String timezoneDisp = chosen.getDisplayName(false, TimeZone.SHORT);

            zId = chosen.toZoneId();
            abbrev = timezoneDisp;
        }

        String local = ZonedDateTime
          .parse(time)
          .toInstant()
          .atZone(zId)
          .toString()
          .split("\\.")[0];
        
        local = local.replaceFirst("T", " @ ");
        
        return local + " " + abbrev;
    }

    private static boolean validateData(Map<String, Object> dataSource) {
        if (dataSource.size() >= numData) {
            String prefered = "";

            try {
                SearchSettings searchSettings = new SearchSettings();

                prefered = Boolean.valueOf(searchSettings.get("local")) ? TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) : searchSettings.get("timezone");
            } catch (IOException ioex) {
                ErrorHandler.report(ioex);
                // note: the dateLocalTime method could handle if it was fed incorrect input due to an io exception, but if
                // an io exception occurs then it is best to inform the user as something is most likely very wrong
            }

            String creationDate = (String) dataSource.get("created");
            creationDate = dateLocalTime(creationDate, prefered);
            dataSource.replace("created", creationDate);

            boolean beaned = (boolean) dataSource.get("isBanned");
            dataSource.remove("isBanned");
            dataSource.put("banned", beaned);

            boolean online = (boolean) dataSource.get("IsOnline");
            dataSource.remove("IsOnline");
            dataSource.put("online", online);

            String lastOnline = (String) dataSource.get("LastOnline");
            lastOnline = dateLocalTime(lastOnline, prefered);
            dataSource.remove("LastOnline");
            dataSource.put("lastonline", lastOnline);

            long id = Long.valueOf(dataSource.get("id").toString());
            dataSource.replace("id", id);

            String dispname = (String) dataSource.get("displayName");
            dataSource.remove("displayName");
            dataSource.put("dispname", dispname);

            if (dataSource.size() > numData) {
                Map<String, Object> dataSrcCopy = new HashMap<>();
                dataSrcCopy.putAll(dataSource);
    
                Set<String> validKeys = Player.getValidKeys();
                Set<String> receivedKeys = dataSrcCopy.keySet();
    
                receivedKeys.removeAll(validKeys);
    
                receivedKeys.forEach((key) -> {
                    System.out.println("Removed unexpected key: " + key);
                    dataSource.remove(key);
                });
            }

            return true;
        }

        return false;
    }

    public static Map<String, Object> searchByUsername(String username) throws UserNotFoundException {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Long> id = exec.submit(() -> {
            try {
                String user = username.replace(' ', '+'); // format properly for http requests

                Link info = new Link("https://api.roblox.com/users/get-by-username?username=" + user);

                String Id = info.data.get("Id").toString();

                return Long.valueOf(Id);
            } catch (IOException e) {
                String stringified = e.toString();

                if (stringified.contains("400")) // http response code 400
                    throw new UserNotFoundException("Failed to fetch user! Code: 400");
                else if (stringified.contains("429")) // http response code 429
                    throw new UserNotFoundException("Too many requests! Code: 429");

                ErrorHandler.report(e);
            }
            
            return 1L;
        });

        Map<String, Object> requested = new HashMap<>();

        long Id = 1L;

        try {
            Id = id.get(5, TimeUnit.SECONDS);
            requested = getInformation(Id);
        } catch (InterruptedException | TimeoutException | SocketTimeoutException e) {
            throw new UserNotFoundException("Failed to fetch user!");
        } catch (ExecutionException executionException) {
            Throwable cause = executionException.getCause();

            String toThrow = (cause != null) ? cause.getMessage() : "Failed to fetch user!";
            
            throw new UserNotFoundException(toThrow);
        }

        return requested;
    }

    public static Map<String, Object> getInformation(long userId) throws SocketTimeoutException, UserNotFoundException {
        Map<String, Object> data = new HashMap<>();

        final String base = "https://friends.roblox.com/v1/users/" + userId;

        final String[] apiDomains = {
            "https://users.roblox.com/v1/users/" + userId,
            base + "/friends/count",
            base + "/followers/count",
            base + "/followings/count",
            "https://api.roblox.com/users/" + userId + "/onlinestatus/"
        };

        final String[][] toFilter = {
            new String[]{"externalAppDisplayName", "hasVerifiedBadge"},
            null,
            null,
            null,
            new String[]{"GameId", "LastLocation", "LocationType", "PlaceId", "VisitorId", "PresenceType", "UniverseId", "Visibility"}
        };

        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        // Start of multi-threaded data retrieval
        Stack<Integer> buffer = new Stack<>();

        final int maxThreads = chosen;

        ThreadPoolExecutor retrieve = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
        Throwable error = new Exception(); // initialize to empty exception, set to usernotfoundexception if error occurs inside the fetch block

        int index = 0;

        for (final String domain : apiDomains) {
            int ind = index;

            Future<Map<String, Object>> fetched = retrieve.submit(() -> {
                Map<String, Object> toReturn = new HashMap<>();

                try {
                    Link con = new Link(domain);

                    toReturn = con.filter(toFilter[ind]);

                    buffer.push(1); // fill buffer upon thread completion

                } catch (IOException e) {
                    int remainingBuffer = 5 - buffer.size();

                    for (int i = 0; i < remainingBuffer; i++)
                        buffer.push(1);
                    
                    if (e.getMessage().contains("429"))
                        throw new UserNotFoundException("Too many requests! Code: 429");
                    
                    return toReturn;
                }

                return toReturn;
            });

            retrieve.submit(() -> {
                try {
                    Map<String, Object> result = fetched.get(3, TimeUnit.SECONDS);
                    
                    if (result.size() < 1)
                        buffer.push(1);

                    data.putAll(result);
                } catch (ExecutionException exc) {
                    Throwable cause = exc.getCause();

                    if (exc.getCause() instanceof UserNotFoundException)
                        error.initCause(cause);
                    else
                        ErrorHandler.report(exc);
                } catch (InterruptedException | TimeoutException timeout) {}
            });

            index++;
        }

        data.put("image", getAppearance.retrieveImage(userId));

        while (buffer.size() < apiDomains.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        retrieve.shutdownNow();
        //End of multi-threaded data retrieval
        if (error.getCause() instanceof UserNotFoundException)
            throw new UserNotFoundException(error.getCause().getMessage());

        boolean valid = validateData(data);

        if (!valid)
            throw new UserNotFoundException("Failed to fetch user! Received " + (numData - data.size()) + " less keys");

        return data;
    }

    public static long getNewestUser(long startingId, long increment) throws IOException {
        long[] idList = new long[10];
        int i = 0;

        long max = startingId;
        boolean foundInitialMax = false;

        while (!foundInitialMax) {
            int lim = i + 10;

            for (; i < lim; i++)
                idList[i % 10] = startingId + increment * (i + 1);
            
            HashMap<String, Object> query = new HashMap<>();
            query.put("userIds", idList);
            query.put("excludeBannedUsers", false);

            String jsonQuery = new JSONObject(query).toString();

            Link endpoint = new Link("https://users.roblox.com/v1/users", jsonQuery, false);
            JSONArray returned = new JSONObject(endpoint.getRawResponse(false)).getJSONArray("data");

            if (returned.length() > 0) {
                JSONObject lastEntry = (JSONObject) returned.get(returned.length() - 1);
                long lastId = lastEntry.getLong("id");

                if (lastId - startingId == 10 * increment) {
                    foundInitialMax = false;
                } else {
                    foundInitialMax = true;
                    max = lastId;
                }
            } else {
                foundInitialMax = true;
            }
        }

        long difference = 2 * increment;
        long upperBound = max + difference; // add 2 * increment in case the ids are very close to surpassing 1 * increment

        while (difference != 1) {
            int log2 = (int) (Math.log(difference) / Math.log(2)); // log2(num)

            long[] halfwayPoints = new long[log2];

            for (i = 1; i <= log2; i++) 
                halfwayPoints[i - 1] = max + (long) ((double) difference / Math.pow(2, i));
            
            HashMap<String, Object> query = new HashMap<>();
            query.put("userIds", halfwayPoints);
            query.put("excludeBannedUsers", false);

            String jsonQuery = new JSONObject(query).toString();

            Link endpoint = new Link("https://users.roblox.com/v1/users", jsonQuery, false);
            JSONArray returned = new JSONObject(endpoint.getRawResponse(false)).getJSONArray("data");

            if (returned.length() > 0) {
                JSONObject greatestEntry = (JSONObject) returned.get(0);
                max = greatestEntry.getLong("id");

                difference -= upperBound - max;
                /* max = previous max + difference/2^n, upperbound = previous max + difference
                ergo, upperbound - max = (previous max + difference) - (previous max + difference/2^n) = difference - difference/2^n
                difference = difference - (difference - difference/2^n) = difference/2^n -> will tend to 1 */
                
                upperBound = max + difference; 
                /* following from our previous logic, if max is previous max + difference/2^n, then we can safely assume
                it does not surpass previous max + 2 * difference/2^n */
            }
        } // essentially just binary search

        return max;
    } // definitely not the cleanest code i've written
}