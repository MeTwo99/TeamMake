import java.util.ArrayList;

public class Team {
    private final ArrayList<Person> members = new ArrayList<>();
    private final int teamNumber;
    private boolean mutable = true;

    public Team(int teamNumber){
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, String[] names) {
        this.teamNumber = teamNumber;
        for(String n : names){
            addMember(Main.getPersonByName(n));
        }
        mutable = false;
    }

    public int getScoreIfPersonAdded(Person p){
        addMember(p);
        int score = getScore();
        removeMember(p);
        return score;
    }

    public void addMember(Person p){
        if(!mutable){
            System.out.println("Warning: Team " + this + " is not mutable.");
            return;
        }
        if (!members.contains(p))
            members.add(p);
    }

    public void removeMember(Person p){
        if(!mutable){
            System.out.println("Warning: Team " + this + " is not mutable.");
            return;
        }
        members.remove(p);
    }

    public int getScore(){
        int score = 0;
        for (Person p : members){
            for (Person other : members) {
                if (p.prefers(other))
                    score++;
            }
        }
        return score;
    }

    @Override
    public String toString() {

        String namesString = "";
        for(Person p : members){
            namesString += p.getName() + ", ";
        }
        namesString = namesString.substring(0, namesString.length()-2);
        return "Team " + teamNumber + ": " + namesString;
    }

    public boolean canAdd(Person toAdd) {
        for (Person p : members){
            if(p.avoids(toAdd))
                return false;
            if(toAdd.avoids(p))
                return false;
        }
        return true;
    }

    public boolean equals(Team o) {
        //if this team has the same members as another team
        for(Person thisPerson : members){
            //does this person exist in the other team?
            boolean hasMatch = false;
            for(Person otherPerson : o.members){
                if(thisPerson == otherPerson){
                    hasMatch = true;
                    break;
                }
            }
            if(!hasMatch)
                return false;
        }
        return true;
    }

    public ArrayList<Person> getPeople() {
        return members;
    }
}
