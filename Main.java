import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static final int TOTAL_RUNS = 100000;
    private static final int RETURN_RESULTS = 5;
    private static final int ALGORITHM_USED = 0;

    public static HashMap<String, Person> nameHash = new HashMap<>();
    public static ArrayList<ArrayList<Team>> bestTeams = new ArrayList<>();
    public static ArrayList<Team> teams = new ArrayList<>();
    private static int maxTeamSize, numOfTeams;
    private static Random rand = new Random();

    public static void main(String [] args){
        File f = new File("./src/team_preferences.txt");

        try {
            Scanner s = new Scanner(f);

            boolean first = true;

            while(s.hasNextLine()){
                String line = s.nextLine();

                if (first){
                    first = false;
                    maxTeamSize = Integer.parseInt(line);
                    continue;
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //show the people for this file
        for(Person p : nameHash.values()){
            System.out.println(p);
        }

        //calc num of teams based on people count and max number in a team
        numOfTeams = (nameHash.size()/maxTeamSize)+1;
        System.out.println("There are " + nameHash.size() + " people in " + numOfTeams + " teams.");

        for (int i = 0; i < TOTAL_RUNS; i++) {
            boolean success = true;
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
            printTeams(bestTeams.get(i));
        }
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
        ArrayList<Team> teamCopy = new ArrayList<>();
        for(Team t : teams){
            teamCopy.add(t);
        }
        int newTeamScore = getTeamsScore(teams);

        //check that this team isnt any of the other teams
        for (int i = 0; i < bestTeams.size(); i++) {
            if (teamSetsEquivalent(teamCopy, bestTeams.get(i)))
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
        int currTeamIndex = 0;
        int stuck = 0;
        ArrayList<Person> people = new ArrayList<Person>(nameHash.values());
        teams = new ArrayList<>();
        for (int i = 0; i < numOfTeams; i++) {
            teams.add(new Team(i));
        }

        while(people.size() > 0){
            Team currTeam = teams.get(currTeamIndex);
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
            }else{
                if(++stuck > numOfTeams)
                    return false;
            }

            //go to the next team, looping around round robin style
            currTeamIndex = (currTeamIndex+1)%numOfTeams;
        }
        return true;
    }

    private static void randomAssignmentAlgorithm() {
        int currTeamIndex = 0;
        ArrayList<Person> people = new ArrayList<Person>(nameHash.values());
        teams = new ArrayList<>();
        for (int i = 0; i < numOfTeams; i++) {
            teams.add(new Team(i));
        }

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
            //this person hasnt been added
            Person p = new Person(name);
            nameHash.put(name, p);
            return p;
        } else
            return nameHash.get(name);
    }

    public static void printTeams(ArrayList<Team> teams){
        System.out.println("Total score: " + getTeamsScore(teams));
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
                if (teamCheck.equals(t2)){
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
