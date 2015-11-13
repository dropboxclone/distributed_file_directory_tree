package dfs;

//import com.hazelcast.core.*;
//import com.hazelcast.config.*;
  
public class Demo{
    public static void main(String[] args) {
        //Config cfg = new Config();
        //HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        Folder root = new Folder(".",".");
        System.out.println("Current Directory : ");
        System.out.println(root);
        boolean isCreated = root.createSubFolder("subFolder1");
        if(!isCreated)
            System.out.println("Cannot create 'subFolder1'! Already Present in root!");
        else
            System.out.println("Successfully created 'subFolder1' in root!");
        System.out.println("Current Directory : ");
        System.out.println(root);
    }
}