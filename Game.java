import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.*;
/**
 * Write a description of class Game here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Game {
    // Constants
    private final String URL = "http://colorfight.herokuapp.com/";
    
    // Object variables
    public boolean data = false;
    public String token;
    public String name;
    public int uid;
    public double currTime = 0;
    public double endTime = 0;
    public double joinEndTime = 0;
    public double planStartTime = -1;
    public double lastUpdate = 0;
    public ArrayList<User> users = new ArrayList<User>();
    public Cell cells[] = new Cell[ 900 ];
    public int cellNum = 0;
    public int baseNum = 0;
    public int goldCellNum = 0;
    public int energyCellNum = 0;
    public double cdTime = 0;
    public double buildCdTime = 0;
    public double energy = 0;
    public double gold = 0;
    public int width = 30;
    public int height = 30;
    
    private String refreshData = "";
    private int refreshIndex = 0;
    private char thisParseChar = ' ';
    private boolean onParseKey = false;
    private String parseKey = "";
    
    // Temporary Cell Data
    private int ownerT;
    private int attackerT;
    private boolean isTakingT;
    private int xT;
    private int yT;
    private double occupyTimeT;
    private double attackTimeT;
    private double takeTimeT;
    private double finishTimeT;
    private String cellTypeT;
    private boolean isBaseT;
    private boolean isBuildingT;
    private double buildTimeT;
    
    // Temporary User Data
    public int idT;
    public String nameT;
    public double cdTimeT;
    public double buildCdTimeT;
    public int cellNumT;
    public int baseNumT;
    public int goldCellNumT;
    public int energyCellNumT;
    public double energyT;
    public double goldT;
    
    /**
     * Constructor for objects of class Game 
     */
    public Game() {
        for( int i = 0; i < cells.length; i++ ) {
            cells[ i ] = new Cell();
        }
    }
    
    public String PostData( String sub, String data ) {
        String responseText = "";
        try {
            URL url = new URL( URL + sub );
            URLConnection conn = url.openConnection();
            HttpURLConnection http = ( HttpURLConnection ) conn;
            http.setRequestMethod( "POST" );
            http.setDoOutput( true );
            byte[] out = data.getBytes( StandardCharsets.UTF_8 );
            http.setFixedLengthStreamingMode( out.length );
            http.setRequestProperty( "Content-Type", "application/json;charset=UTF-8" );
            http.connect();
            try( OutputStream os = http.getOutputStream() ) {
                os.write( out );
                os.close();
            } catch( Exception e ) {
                System.out.println( e );
            }
            String decodedString;
            try( BufferedReader in = new BufferedReader( new InputStreamReader( http.getInputStream() ) ) ) {
                while( ( decodedString = in.readLine() ) != null ) {
                    responseText += decodedString;
                }
                in.close();
            } catch( Exception e ) {
                System.out.println( e );
            }
        } catch ( Exception e ) {
            System.out.println( e );
        }
        return responseText;
    }
    
    public boolean JoinGame( String name ) {
        File tokenFile = new File( "token" );
        String responseText;
        if( tokenFile.exists() ) {
            try {
                BufferedReader reader = new BufferedReader( new FileReader( "token" ) );
                String tempToken = "";
                String line;
                while( ( line = reader.readLine() ) != null ) {
                    tempToken += line;
                }
                reader.close();
                responseText = PostData( "checktoken", "{\"token\":\"" + tempToken.trim() + "\"}" );
                if( responseText != "" ) {
                    String tempName = responseText.split( "\"name\":\"" )[ 1 ].split( "\"" )[ 0 ];
                    if( tempName.trim().equals( name.trim() ) ) {
                        this.name = tempName.trim();
                        String tempUid = responseText.split( "\"uid\":" )[ 1 ];
                        char uidChar = ' ';
                        String uidStr = "";
                        for( int i = 0; i < tempUid.length(); i++ ) {
                            uidChar = tempUid.charAt( i );
                            if( ( ( int ) uidChar ) >= 48 && ( ( int ) uidChar ) <= 57 ) {
                                uidStr += uidChar;
                            } else {
                                break;
                            }
                        }
                        this.token = tempToken.trim();
                        this.uid = Integer.parseInt( uidStr );
                        return true;
                    }
                }
            } catch( Exception e ) {
                System.out.println( e );
            }
        } else {
            try {
                tokenFile.createNewFile();
            } catch( Exception e ) {
                System.out.println( e );
            }
        }
        responseText = PostData( "joingame", "{\"name\":\"" + name.trim() + "\"}" );
        if( responseText != "" ) {
            String token = responseText.split( "\"token\":\"" )[ 1 ].split( "\"" )[ 0 ];
            try {
                FileOutputStream fos = new FileOutputStream( tokenFile, false );
                OutputStreamWriter osw = new OutputStreamWriter( fos, "UTF-8" );
                BufferedWriter bw = new BufferedWriter( osw );
                PrintWriter pw = new PrintWriter( bw, true );
                pw.write( token );
                pw.close();
                bw.close();
                osw.close();
                fos.close();
            } catch( Exception e ) {
                System.out.println( e );
            }
            String tempUid = responseText.split( "\"uid\":" )[ 1 ];
            char uidChar = ' ';
            String uidStr = "";
            for( int i = 0; i < tempUid.length(); i++ ) {
                uidChar = tempUid.charAt( i );
                if( ( ( int ) uidChar ) >= 48 && ( ( int ) uidChar ) <= 57 ) {
                    uidStr += uidChar;
                } else {
                    break;
                }
            }
            this.uid = Integer.parseInt( uidStr );
            this.token = token;
            return true;
        }
        return false;
    }
    
    public void UntilChar( char c ) {
        for( ; refreshData.charAt( refreshIndex ) != c; refreshIndex ++ );
    }
    
    public int NextInt() {
        String intStr = "";
        UntilChar( ':' );
        refreshIndex++;
        for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}' && thisParseChar != ','; refreshIndex++ ) {
            intStr += thisParseChar;
        }
        return Integer.parseInt( intStr );
    }
    
    public String NextString() {
        String str = "";
        UntilChar( ':' );
        UntilChar( '"' );
        refreshIndex++;
        for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '"'; refreshIndex++ ) {
            str += thisParseChar;
        }
        refreshIndex++;
        return str;
    }
    
    public double NextDouble() {
        String doubleStr = "";
        UntilChar( ':' );
        refreshIndex++;
        for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}' && thisParseChar != ','; refreshIndex++ ) {
            doubleStr += thisParseChar;
        }
        return Double.parseDouble( doubleStr );
    }
    
    public boolean NextBool() {
        String boolStr = "";
        UntilChar( ':' );
        refreshIndex++;
        for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}' && thisParseChar != ','; refreshIndex++ ) {
            boolStr += thisParseChar;
        }
        return Boolean.parseBoolean( boolStr );
    }
    
    public void ParseUsers() {
        User u;
        users = new ArrayList<User>();
        UntilChar( '[' );
        refreshIndex++;
        while( ( thisParseChar = refreshData.charAt( refreshIndex ) ) != ']' ) {
            u = new User();
            UntilChar( '{' );
            onParseKey = false;
            for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}'; refreshIndex++ ) {
                if( thisParseChar == '"' ) {
                    if( onParseKey ) {
                        onParseKey = false;
                        if( parseKey.equals( "id" ) ) {
                            this.idT = NextInt();
                        } else if( parseKey.equals( "name" ) ) {
                            this.nameT = NextString();
                        } else if( parseKey.equals( "cd_time" ) ) {
                            this.cdTimeT = NextDouble();
                        } else if( parseKey.equals( "build_cd_time" ) ) {
                            this.buildCdTimeT = NextDouble();
                        } else if( parseKey.equals( "cell_num" ) ) {
                            this.cellNumT = NextInt();
                        } else if( parseKey.equals( "base_num" ) ) {
                            this.baseNumT = NextInt();
                        } else if( parseKey.equals( "gold_cell_num" ) ) {
                            this.goldCellNum = NextInt();
                        } else if( parseKey.equals( "energy_cell_num" ) ) {
                            this.energyCellNum = NextInt();
                        } else if( parseKey.equals( "energy" ) ) {
                            this.energy = NextDouble();
                        } else if( parseKey.equals( "gold" ) ) {
                            this.gold = NextDouble();
                        }
                    } else {
                        parseKey = "";
                        onParseKey = true;
                    }
                } else if( onParseKey ){
                    parseKey += thisParseChar;
                }
                if( refreshData.charAt( refreshIndex ) == '}' ) {
                    break;
                }
            }
            u.id = this.idT;
            u.name = this.nameT;
            u.cdTime = this.cdTimeT;
            u.buildCdTime = this.buildCdTimeT;
            u.cellNum = this.cellNumT;
            u.baseNum = this.baseNumT;
            u.goldCellNum = this.goldCellNumT;
            u.energyCellNum = this.energyCellNumT;
            u.energy = this.energyT;
            u.gold = this.goldT;
            if( this.idT == this.uid ) {
                this.cdTime = this.cdTimeT;
                this.buildCdTime = this.buildCdTimeT;
                this.cellNum = this.cellNumT;
                this.baseNum = this.baseNumT;
                this.goldCellNum = this.goldCellNumT;
                this.energyCellNum = this.energyCellNumT;
                this.energy = this.energyT;
                this.gold = this.goldT;
            }
            refreshIndex++;
        }
        /*for( ; refreshIndex < refreshData.length(); refreshIndex++ ) {
            thisParseChar = refreshData.charAt( refreshIndex );
            if( thisParseChar == ']' ) {
                return;
            }
        }*/
    }
    
    public void ParseCells() {
        /*for( ; refreshIndex < refreshData.length(); refreshIndex++ ) {
            thisParseChar = refreshData.charAt( refreshIndex );
            if( thisParseChar == ']' ) {
                return;
            }
        }*/
        Cell c;
        UntilChar( '[' );
        refreshIndex++;
        while( ( thisParseChar = refreshData.charAt( refreshIndex ) ) != ']' ) {
            UntilChar( '{' );
            onParseKey = false;
            for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}'; refreshIndex++ ) {
                if( thisParseChar == '"' ) {
                    if( onParseKey ) {
                        onParseKey = false;
                        if( parseKey.equals( "o" ) ) {
                            this.ownerT = NextInt();
                        } else if( parseKey.equals( "a" ) ) {
                            this.attackerT = NextInt();
                        } else if( parseKey.equals( "c" ) ) {
                            this.isTakingT = NextInt() == 1;
                        } else if( parseKey.equals( "x" ) ) {
                            this.xT = NextInt();
                        } else if( parseKey.equals( "y" ) ) {
                            this.yT = NextInt();
                        } else if( parseKey.equals( "ot" ) ) {
                            this.occupyTimeT = NextDouble();
                        } else if( parseKey.equals( "at" ) ) {
                            this.attackTimeT = NextDouble();
                        } else if( parseKey.equals( "t" ) ) {
                            this.takeTimeT = NextDouble();
                        } else if( parseKey.equals( "f" ) ) {
                            this.finishTimeT = NextDouble();
                        } else if( parseKey.equals( "ct" ) ) {
                            this.cellTypeT = NextString();
                        } else if( parseKey.equals( "b" ) ) {
                            this.isBaseT = NextString().equals( "base" );
                        } else if( parseKey.equals( "bf" ) ) {
                            this.isBuildingT = !NextBool();
                        } else if( parseKey.equals( "bt" ) ) {
                            this.buildTimeT = NextDouble();
                        }
                    } else {
                        parseKey = "";
                        onParseKey = true;
                    }
                } else if( onParseKey ){
                    parseKey += thisParseChar;
                }
                if( refreshData.charAt( refreshIndex ) == '}' ) {
                    break;
                }
            }
            c = this.cells[ this.width * this.yT + this.xT ];
            c.y = this.yT;
            c.x = this.xT;
            c.owner = this.ownerT;
            c.attacker = this.attackerT;
            c.isTaking = this.isTakingT;
            c.occupyTime = this.occupyTimeT;
            c.attackTime = this.attackTimeT;
            c.takeTime = this.takeTimeT;
            c.finishTime = this.finishTimeT;
            c.cellType = this.cellTypeT;
            c.isBase = this.isBaseT;
            c.isBuilding = this.isBuildingT;
            c.buildTime = this.buildTimeT;
            refreshIndex++;
        }
    }
    
    public void ParseInfo() {
        onParseKey = false;
        UntilChar( '{' );
        for( ; ( thisParseChar = refreshData.charAt( refreshIndex ) ) != '}'; refreshIndex++ ) {
            if( thisParseChar == '"' ) {
                if( onParseKey ) {
                    onParseKey = false;
                    //System.out.println( "Info: " + parseKey );
                    if( parseKey.equals( "time" ) ) {
                        this.currTime = NextDouble();
                    } else if( parseKey.equals( "join_end_time" ) ) {
                        this.joinEndTime = NextDouble();
                    }  else if( parseKey.equals( "end_time" ) ) {
                        this.endTime = NextDouble();
                    }  else if( parseKey.equals( "game_version" ) ) {
                        NextString();
                    }  else if( parseKey.equals( "width" ) ) {
                        NextInt();
                    }  else if( parseKey.equals( "height" ) ) {
                        NextInt();
                    }  else if( parseKey.equals( "plan_start_time" ) ) {
                        //this.planStartTime = NextDouble();
                    }  else if( parseKey.equals( "game_id" ) ) {
                        NextInt();
                    } 
                } else {
                    parseKey = "";
                    onParseKey = true;
                }
            } else if( onParseKey ){
                parseKey += thisParseChar;
            }
        }
        return;
    }
    
    public double GetTakeTimeEq( double timeDiff ) {
        if( timeDiff <= 0 ) return 33;
        return 30 * ( double ) ( Math.pow( 2, ( -timeDiff / 30.0f ) ) ) + 3;
    }
    
    public Cell GetCell( int x, int y ) {
        if( x < 0 || x > 29 || y < 0 || y > 29) return null;
        return this.cells[ y * this.width + x ];
    }
    
    public void Refresh() {
        String responseText;
        String key = "";
        char thisChar;
        char lastChar = ' ';
        refreshIndex = 0;
        boolean onKey = false;
        if( this.data ) {
            this.refreshData = PostData( "getgameinfo", "{\"protocol\":1,\"timeAfter\":" + this.lastUpdate + "}" );
        } else {
            this.refreshData = PostData( "getgameinfo", "{\"protocol\":2}" );
        }
        if( this.refreshData != "" ) {
            for( refreshIndex = 0; refreshIndex < this.refreshData.length(); refreshIndex++ ) {
                thisChar = this.refreshData.charAt( refreshIndex );
                if( thisChar == '"' ) {
                    if( onKey ) {
                        if( lastChar == '\\' ) {
                            
                        } else {
                            if( key.equals( "users" ) ) {
                                ParseUsers();
                            } else if( key.equals( "cells" ) ) {
                                ParseCells();
                            } else if( key.equals( "info" ) ) {
                                ParseInfo();
                            }
                            onKey = false;
                        }
                    } else {
                        key = "";
                        onKey = true;
                    }
                } else if( onKey ) {
                    if( thisChar == '\\' ) {
                    } else {
                        key += thisChar;
                    }
                }
                lastChar = thisChar;
            } 
        }
        if( this.data ) {
            for( int i = 0; i < cells.length; i++ ) {
                if( !cells[ i ].isTaking && cells[ i ].owner != 0 ) {
                    cells[ i ].takeTime = GetTakeTimeEq( this.currTime - cells[ i ].occupyTime );
                }
            }
        } else {
            this.data = true;
        }
        this.lastUpdate = this.currTime;
        return;
    }
    
    public int AttackCell( int x, int y ) {
        String responseText = PostData( "attack", "{\"cellx\":" + x + ",\"celly\":" + y + ",\"boost\":false,\"token\":\"" + this.token + "\"}" );
        if( this.token != "" && responseText != "" ) {
            char c = ' ';
            String err_code = "";
            for( int i = responseText.indexOf( "\"err_code\":" ) + 11; ( c = responseText.charAt( i ) ) != ',' && c != '}'; i++ ) {
                err_code += c;
            }
            return Integer.parseInt( err_code );
        } else {
            return -1;
        }
    }
    
    public int AttackCell( int x, int y, boolean boost ) {
        String responseText = PostData( "attack", "{\"cellx\":" + x + ",\"celly\":" + y + ",\"boost\":" + boost + ",\"token\":\"" + this.token + "\"}" );
        if( this.token != "" && responseText != "" ) {
            char c = ' ';
            String err_code = "";
            for( int i = responseText.indexOf( "\"err_code\":" ) + 11; ( c = responseText.charAt( i ) ) != ',' && c != '}'; i++ ) {
                err_code += c;
            }
            return Integer.parseInt( err_code );
        } else {
            return -1;
        }
    }
    
    public int BuildBase( int x, int y ) {
        String responseText = PostData( "buildbase", "{\"cellx\":" + x + ",\"celly\":" + y + ",\"token\":\"" + this.token + "\"}" );
        if( this.token != "" && responseText != "" ) {
            char c = ' ';
            String err_code = "";
            for( int i = responseText.indexOf( "\"err_code\":" ) + 11; ( c = responseText.charAt( i ) ) != ',' && c != '}'; i++ ) {
                err_code += c;
            }
            return Integer.parseInt( err_code );
        } else {
            return -1;
        }
    }
    
    public int Blast( int x, int y, String direction ) {
        String responseText = PostData( "blast", "{\"cellx\":" + x + ",\"celly\":" + y + ",\"token\":\"" + this.token + "\",\"direction\":\"" + direction + "\"}" );
        if( this.token != "" && responseText != "" ) {
            char c = ' ';
            String err_code = "";
            for( int i = responseText.indexOf( "\"err_code\":" ) + 11; ( c = responseText.charAt( i ) ) != ',' && c != '}'; i++ ) {
                err_code += c;
            }
            return Integer.parseInt( err_code );
        } else {
            return -1;
        }
    }
    
    public int MultiAttack( int x, int y ) {
        String responseText = PostData( "multiattack", "{\"cellx\":" + x + ",\"celly\":" + y + ",\"token\":\"" + this.token + "\"}" );
        if( this.token != "" && responseText != "" ) {
            char c = ' ';
            String err_code = "";
            for( int i = responseText.indexOf( "\"err_code\":" ) + 11; ( c = responseText.charAt( i ) ) != ',' && c != '}'; i++ ) {
                err_code += c;
            }
            return Integer.parseInt( err_code );
        } else {
            return -1;
        }
    }
}
