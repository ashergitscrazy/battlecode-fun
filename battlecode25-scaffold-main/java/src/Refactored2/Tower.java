package Refactored2;

import battlecode.common.*;

import java.util.ArrayList;

public class Tower {
    static ArrayList<MapLocation> ruins = new ArrayList<>();
    static int numSoldiers = 0;
    static boolean isSaving = false;
    static int towerCount = 0;
    public static void runTower(RobotController rc) throws GameActionException{
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            int[] coords = RobotPlayer.BitsToLocation(m.getBytes());
            int ruinX = coords[0];
            int ruinY = coords[1];
            System.out.println(ruinX + ", " + ruinY);
            ruins.add(new MapLocation(ruinX, ruinY));
            if(RobotPlayer.gamePhase == 0) {
                RobotPlayer.gamePhase = 1;
            }
        }
        // Pick a direction to build in.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        if(RobotPlayer.gamePhase == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc) && numSoldiers == 0){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            if(rc.canSendMessage(nextLoc, 0)) {
                rc.sendMessage(nextLoc, RobotPlayer.LocationToBits(rc.getLocation()));
            }
            System.out.println("BUILT A SOLDIER");
            numSoldiers ++;
        }
        if(RobotPlayer.gamePhase == 0 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        }
        if(RobotPlayer.gamePhase == 1 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc) && !ruins.isEmpty() && !isSaving){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER for building");
            if(rc.canSendMessage(nextLoc, 0)) {
                rc.sendMessage(nextLoc, RobotPlayer.LocationToBits(ruins.getFirst()));
                System.out.println("sent");
                towerCount ++;
                isSaving = true;
                ruins.removeFirst();
            }
        }
        if(rc.getMoney() > 1000) {
            isSaving = false;
        }
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo enemy: enemyRobots) {
            if(rc.canAttack(enemy.getLocation())) {
                rc.attack(enemy.getLocation());
                System.out.println("Attack!");
            }
        }


        // TODO: can we attack other bots?
    }
}
