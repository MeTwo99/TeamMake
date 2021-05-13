import java.util.ArrayList;

public class Team {
    private ArrayList<Person> members = new ArrayList<>();
    private int teamNumber;

    public Team(int teamNumber){
        this.teamNumber = teamNumber;
    }

    public int getScoreIfPersonAdded(Person p){
        addMember(p);
        int score = getScore();
        removeMember(p);
        return score;
    }
    public void addMember(Person p){
        if (!members.contains(p))
            members.add(p);
    }

    public void removeMember(Person p){
        if (members.contains(p))
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
}
