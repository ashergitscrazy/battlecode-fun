package Refactored2;

import battlecode.common.*;

import java.util.ArrayList;

public class Mopper {
    static MapLocation target;
    static boolean isScout = (RobotPlayer.gamePhase == 0);
    static boolean justSpawned = true;
    static boolean returnHome = false;
    static MapLocation[] spawnTower;
    static ArrayList<MapLocation> knownTowers = new ArrayList<>();
    static int moveCount = 0;
    static Direction previous;
    public static void runMopper(RobotController rc) throws GameActionException {
        int moveLength = getMapSize(rc) / 2000;
        if(justSpawned) {
            spawnTower = rc.senseNearbyRuins(4);
            knownTowers.add(spawnTower[0]);
            justSpawned = false;
        }
        if(isScout) {
            MapLocation[] nearbyRuins = rc.senseNearbyRuins(4);
            for (MapLocation ruin : nearbyRuins) {
                if (!knownTowers.contains(ruin) && checkAround(rc, ruin)) {
                    target = ruin;
                    knownTowers.add(ruin);
                    returnHome = true;
                }
            }

        }
        if(returnHome) {
            Direction dir = rc.getLocation().directionTo(spawnTower[0]);
            if(rc.canMove(dir)) {
                rc.move(dir);
            }
            if(rc.canSendMessage(spawnTower[0], 0)) {
                rc.sendMessage(spawnTower[0], RobotPlayer.LocationToBits(target));
                target = null;
                RobotPlayer.gamePhase = 1;
                returnHome = false;
            }
        }
        if(moveCount == 0) {
            Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            previous = dir;
        }
        if(moveCount > moveLength) {
            moveCount = 0;
        }
        if(rc.canMove(previous)) {
            rc.move(previous);
            moveCount ++;
        }
        if(rc.canMopSwing(previous)) {
            rc.mopSwing(previous);
        }

        // We can also move our code into different methods or classes to better organize it!
        RobotPlayer.updateEnemyRobots(rc);
    }
    public static boolean checkAround(RobotController rc, MapLocation loc) throws GameActionException {
        int count = 0;
        for(Direction d: RobotPlayer.directions) {
            if(rc.senseMapInfo(loc.add(d)).getMark() != PaintType.EMPTY) {
                count ++;
            }
        }
        return count != 8;
    }

    public static int getMapSize(RobotController rc) {
        return rc.getMapHeight() * rc.getMapWidth();
    }
}
