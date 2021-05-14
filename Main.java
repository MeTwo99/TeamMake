import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static final int TOTAL_RUNS = 500000; //increasing this will make the program take longer
    private static final int RETURN_RESULTS = 10; //increasing this will make the program take longer
    private static final int ALGORITHM_USED = 0;
    private static final String [] TEAM_PREFERENCES_FILE = {"./src/team_preferences.txt","./team_preferences.txt"};

    public static HashMap<String, Person> nameHash = new HashMap<>();
    public static ArrayList<ArrayList<Team>> bestTeams = new ArrayList<>();
    public static ArrayList<Team> teams = new ArrayList<>(), keepTeams = new ArrayList<>();
    public static ArrayList<Person> people;
    public static double startTime;

    private static int maxTeamSize, numOfTeams;
    private static final Random rand = new Random();

    public static void main(String [] args){
        //attempt to find a file from args or normal places
        File f = null;
        if(args.length > 0)
            f = new File(args[0]);
        else{
            int i = 0;
            while((f == null || !f.exists()) && i < TEAM_PREFERENCES_FILE.length){
                f = new File(TEAM_PREFERENCES_FILE[i]);
                i++;
            }
        }

        try {
            Scanner s = new Scanner(f);
            System.out.println("Using file:" + f.getAbsolutePath());

            boolean first = true, setTeams = false;

            while(s.hasNextLine()){
                String line = s.nextLine();

                if (first){
                    first = false;
                    try{
                        maxTeamSize = Integer.parseInt(line);
                        continue;
                    }
                    catch (NumberFormatException nfe){
                        System.out.println("First line should contain the max team size. Using max team size 3");
                        maxTeamSize = 3;
                    }
                }

                if(line.equals("---")){
                    setTeams = true;
                    break;
                }
                String [] names = line.split(",");
                Person thisPerson = getPersonByName(sanitizeString(names[0]));
                //for each person, add their preferences
                for(int i = 1; i < names.length; i++){
                    String thisName = sanitizeString(names[i]);
                    if(thisName.charAt(0) == '-'){
                        thisName = thisName.substring(1);
                        thisPerson.avoid(thisName);
                    }else{
                        thisPerson.addPreference(thisName);
                    }
                }
            }

            //if broken to set teams, read set team data now
            if (setTeams) {
                //teams that are always the same
                int i = 0;
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    String [] names = line.split(",");
                    keepTeams.add(new Team(i++, names));
                }
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //show the people for this file
        System.out.println("Team members and preferences list:");
        for(Person p : nameHash.values()){
            System.out.println(p);
        }

        //show the teams to always keep in the groups
        int keptPeople = 0;
        if(keepTeams.size() > 0){
            System.out.println("\nKeep teams:");
            for(Team t : keepTeams){
                keptPeople += t.getPeople().size();
                System.out.println(t);
            }
        }

        //calc num of teams based on people count and max number in a team
        numOfTeams = keepTeams.size() + ((nameHash.size()-keptPeople)/maxTeamSize)+1;
        System.out.println("\nThere are " + nameHash.size() + " people in " + numOfTeams + " teams of " + maxTeamSize + "(max).\n");
        startTime = System.nanoTime();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            boolean success = true;
            //init resources for algorithm
            //create teams
            //give people
            people = new ArrayList<>(nameHash.values());
            for(Team team : keepTeams){
                people.removeAll(team.getPeople());
            }
            teams = new ArrayList<>();
            teams.addAll(keepTeams);
            for (int teamIndex = keepTeams.size(); teamIndex < numOfTeams; teamIndex++) {
                teams.add(new Team(teamIndex));
            }

            switch (ALGORITHM_USED){
                case 0:
                    success = roundRobinAlgorithm();
                    break;
                case 1:
                    randomAssignmentAlgorithm();
                    break;
            }
            if (success)
                addTeamsToBestTeams();
        }

        //print the resulting best teams
        for (int i = 0; i < RETURN_RESULTS; i++) {
            System.out.print("Set " + i + ": ");
            printTeams(bestTeams.get(i));
            System.out.println();
        }

        double time = (System.nanoTime()-startTime)/1000000000;
        System.out.println("[In " + time + " seconds]");
    }

    private static String sanitizeString(String s){
        s = s.toLowerCase();
        while(s.charAt(0) == ' '){
            s = s.substring(1,s.length()-1);
        }
        while(s.charAt(s.length()-1) == ' '){
            s = s.substring(0,s.length()-2);
        }
        return s;
    }

    private static void addTeamsToBestTeams() {
        ArrayList<Team> teamCopy = new ArrayList<>(teams);
        int newTeamScore = getTeamsScore(teams);

        //check that this team isn't any of the other teams
        for (ArrayList<Team> bestTeam : bestTeams) {
            if (teamSetsEquivalent(teamCopy, bestTeam))
                return; //if it is equivalent to one of the teams, skip it
        }

        if(bestTeams.size() < RETURN_RESULTS){
            bestTeams.add(teamCopy);
        }else{
            //check it against these
            for (int i = 0; i < RETURN_RESULTS; i++) {
                if (getTeamsScore(bestTeams.get(i)) < newTeamScore) { //could save on calculations here if we saved the scores
                    bestTeams.remove(i);
                    bestTeams.add(teamCopy);
                    return;
                }
            }
        }
    }

    private static boolean roundRobinAlgorithm() {
        int currTeamIndex = keepTeams.size();
        int stuck = 0;

        while(people.size() > 0 && stuck <= numOfTeams){
            Team currTeam = teams.get(currTeamIndex);

            if(currTeam.getPeople().size() >= maxTeamSize){
                stuck++;
                continue; //skip teams with the max number of members already
            }


            int maxScore = 0;
            ArrayList<Person> bestPeople = new ArrayList<>();

            //get the best people to add
            for(Person p : people){
                if (!currTeam.canAdd(p))
                    continue;

                int newScore = currTeam.getScoreIfPersonAdded(p);
                if (newScore > maxScore){
                    maxScore = newScore;
                    bestPeople = new ArrayList<>();
                    bestPeople.add(p);
                } else if (newScore == maxScore){
                    bestPeople.add(p);
                }
            }

            //add one of the best people to this team and remove them from the list
            if(bestPeople.size() > 0){
                Person selectedPerson = bestPeople.get(rand.nextInt(bestPeople.size()));
                currTeam.addMember(selectedPerson);
                people.remove(selectedPerson);
            }else
                ++stuck;

            //go to the next team, looping around round robin style, skipping kept team indexes
            currTeamIndex = (currTeamIndex+1)%numOfTeams;
            currTeamIndex = (currTeamIndex == 0 ? keepTeams.size() : currTeamIndex);
        }
        return stuck <= numOfTeams; //team creation failed if it got stuck too many times
    }

    private static void randomAssignmentAlgorithm() {
        int currTeamIndex = 0;

        while(people.size() > 0){
            Team currTeam = teams.get(currTeamIndex);

            //add a random person to this team and remove them from the list
            Person selectedPerson = people.get(rand.nextInt(people.size()));
            currTeam.addMember(selectedPerson);
            people.remove(selectedPerson);

            //go to the next team, looping around round robin style
            currTeamIndex = (currTeamIndex+1)%numOfTeams;
        }
    }

    public static Person getPersonByName(String name) {
        if (!nameHash.containsKey(name)){
            //this person hasn't been added
            Person p = new Person(name);
            nameHash.put(name, p);
            return p;
        } else
            return nameHash.get(name);
    }

    public static void printTeams(ArrayList<Team> teams){
        System.out.println("Score " + getTeamsScore(teams));
        for(Team t : teams){
            System.out.println(t);
        }
    }

    public static int getTeamsScore(ArrayList<Team> teams){
        int totalScore = 0;
        for(Team t : teams){
            totalScore += t.getScore();
        }
        return totalScore;
    }

    public static boolean teamSetsEquivalent(ArrayList<Team> t1, ArrayList<Team> t2){
        //for each team
        for(Team teamCheck : t1){
            //each team must equal some other team
            boolean hasMatch = false;
            for(Team teamOther : t2){
                if (teamCheck.equals(teamOther)){
                    hasMatch = true;
                    break;
                }
            }
            if(!hasMatch)
                return false;
        }
        return true;
    }
}
