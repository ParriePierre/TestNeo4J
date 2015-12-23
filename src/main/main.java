package main;

import Grapher.GraphHandler;
import org.neo4j.graphdb.Node;

/**
 * Created by parrie on 22/12/15.
 */
public class main {

        public static void main(String [] args)
    {
        GraphHandler gh = new GraphHandler();
        gh.connectToDB();
        gh.add5NodeToGraph();

        Node n1;
        Node n2;

        for(int i=0; i<4; i++)
        {
            n1= gh.findUsersById("myID"+i);
            for(int j = i+1; j<=4 ;j++)
            {
                n2= gh.findUsersById("myID"+j);
                gh.addRelationship(n1,n2);
                System.out.print("Created relation between ");
                gh.printNode(n1);
                System.out.print("and ");
                gh.printNode(n2);
                System.out.println();
            }
        }

        gh.shutConnection();
    }
}
