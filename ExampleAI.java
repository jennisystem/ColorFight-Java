import java.util.*;

public class ExampleAI
{
    public static void main( String[] args ) {
        int directions[][] = new int[][]{ new int[] { -1, 0 }, new int[] { 1, 0 }, new int[] { 0, -1 },new int[] { 0, 1 } };  
        // Instantiate a Game object.
        Game g = new Game();
        // You need to join the game using JoinGame(). 'MyAI' is the name of your
        // AI, you can change that to anything you want. This function will generate
        // a token file in the folder which preserves your identity so that you can
        // stop your AI and continue from the last time you quit. 
        // If there's a token and the token is valid, JoinGame() will continue. If
        // not, you will join as a new player.
        if( g.JoinGame( "MyAI" ) ) { 
            //Put you logic in a while True loop so it will run forever until you 
            // manually stop the game
            while( true ) {
                // Refresh the game, get updated game data
                g.Refresh();
                // Use a nested for loop to iterate through the cells on the map
                for( int x = 0; x < g.width; x++ ) {
                    for( int y = 0; y < g.width; y++ ) {
                        // Get a cell
                        Cell c = g.GetCell( x, y );
                        // If the cell I got is mine
                        if( c.owner == g.uid ) {
                            // Pick a random direction based on current cell 
                            int[] d = directions[ ( int ) ( Math.random() * 4 ) ];
                            // Get that adjacent cell
                            Cell cc = g.GetCell( x + d[ 0 ], y + d[ 1 ] );
                            // If that cell is valid(current cell + direction could be
                            // out of range) and that cell is not mine
                            if( cc != null && cc.owner != g.uid ) {
                                // Attack the cell and print the error code
                                System.out.println( g.AttackCell( cc.x, cc.y ) );
                            }
                        }
                    }
                }
            }
        }
    }
}
