/*
 * Datastruture for a b3 level
 */
import java.util.Comparator;

public class B3Level implements Comparator<B3Level> {
    private String groupbits, name, keyword, level;
    
    /**
     * 
     * @param groupbits string representation of the int id from the `groups` table
     * @param name name of the group. ex Guest, Admin, SuperAdmin, etc
     * @param keyword keyword for the group name
     * @param level string representation of the int level
     */
    public B3Level(String groupbits, String name, String keyword, String level) {
        this.groupbits = groupbits;
        this.name = name;
        this.keyword = keyword;
        this.level = level;
    }
    
    public String getGroupbits() {
        return groupbits;
    }
    
    public int getbits() {
        int bits;
        
        try {
            bits = Integer.parseInt(groupbits);
        }
        catch (NumberFormatException e) {
            bits = 0;
        }
        
        return bits;
    }
    
    public String getName() {
        return name;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getLevel() {
        return level;
    }
    
    @Override
    public String toString() {
        return groupbits + "::" + name + "::" + keyword + "::" + level;
    }
    
    @Override
    public int compare(B3Level l1, B3Level l2) {
        return l1.getbits() - l2.getbits();
    }
}
