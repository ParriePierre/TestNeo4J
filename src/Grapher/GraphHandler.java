package Grapher;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Created by parrie on 23/12/15.
 */
public class GraphHandler {

    /**
     * MODELE DE LA DB:
     * Node: TwitterAccount(id), screenname
     * relationship: Friend
     */

    /**
     * Connection to DB
     */
    GraphDatabaseService graphDb;

    private static enum RelTypes implements RelationshipType
    {
        Friend
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    /**
     * Ajoute un node (compte twitter) au graphe
     * @param id ID du compte twitter
     * @param screenname Nom du propriiétaire du compte
     */
    public void addNodeToGraph(String id, String screenname) {
        try ( Transaction tx = graphDb.beginTx() )
        {
            Label labelID = DynamicLabel.label( "id" );

            Node userNode = graphDb.createNode( labelID );
            userNode.setProperty( "TwitterAccount", id);
            userNode.setProperty( "screenname", screenname);

            tx.success();
        }
    }

    /**
     * Respecte les transactions
     * @param n
     */
    public void printNode(Node n) {
        try ( Transaction tx = graphDb.beginTx() )
        {
            System.out.print(n.getProperty("screenname")+ " ");
            System.out.println(n.getProperty("TwitterAccount"));
        }
    }

    /**
     * Trouve un compte dans le graphe grâce à son ID Twitter
     * @param ID L'ID Twitter du compte à retrouver
     * @return Le node du graphe correspondant au compte, ou null
     */
    public Node findUsersById(String ID) {
        Label labelID = DynamicLabel.label( "id" );
        Node userNodes = null;

        try ( Transaction tx = graphDb.beginTx() )
        {
            try ( ResourceIterator<Node> users = graphDb.findNodes( labelID, "TwitterAccount", ID ) )
            {
                if(users.hasNext())
                    userNodes = (Node) users.next();
            }
        }
        return userNodes;
    }

    /**
     * Créer toujours les 5 memes utilisateurs
     */
    public void add5NodeToGraph(){
        try ( Transaction tx = graphDb.beginTx() )
        {
            Label labelID = DynamicLabel.label( "id" );
            String[] names = {"ParriePierre","FanjeauxBenjamin", "LemeePaul", "DeverlyAntoine","DavideDrago"};

            // Create some users
            for ( int id = 0; id < 5; id++ )
            {
                Node userNode = graphDb.createNode( labelID );
                userNode.setProperty( "TwitterAccount", "myID" + id);
                userNode.setProperty( "screenname", names[id]);
            }
            System.out.println( "Users created" );
            tx.success();
        }
    }

    /**
     * Créer la relation de 1 vers 2
     * @param n1
     * @param n2
     */
    public void addRelationship(Node n1, Node n2)
    {
        try ( Transaction tx = graphDb.beginTx() )
        {
            Relationship r = n1.createRelationshipTo(n2, RelTypes.Friend);
            tx.success();
        }
    }

    /**
     * A MODIFIER:
     * L'adresse de la DB est rentrée en absolu -> utiliser fichier de conf
     *
     */
    public void connectToDB() {
        File DB_PATH = new File("/home/parrie/Documents/PFE/neo4j-community-2.3.1/data/graph.db");
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        registerShutdownHook( graphDb );

        IndexDefinition indexDefinition;
        IndexDefinition indexDefinition2;

        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor( DynamicLabel.label( "id" ) )
                    .on( "TwitterAccount" )
                    .create();
            schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
        } catch (ConstraintViolationException e) {
            System.err.println("DB already defined");
        }
    }

    public void shutConnection()
    {
        graphDb.shutdown();
    }
}
