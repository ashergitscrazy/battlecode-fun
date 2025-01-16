package Refactored2;

import battlecode.common.*;

import java.util.ArrayList;

public class Soldier {
    static MapLocation target;
    static Direction previous;
    static int moveCount= 0;
    static boolean justSpawned = true;
    static MapLocation[] spawnTower;
    static ArrayList<MapLocation> knownTowers = new ArrayList<>();
    public static void runSoldier(RobotController rc) throws GameActionException {
        int moveLength = getMapSize(rc) / 1000;
        if(justSpawned) {
            spawnTower = rc.senseNearbyRuins(4);
            knownTowers.add(spawnTower[0]);
            justSpawned = false;
        }
        Message[] messages = rc.readMessages(-1);
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        for (Message m : messages) {
            int[] coords = RobotPlayer.BitsToLocation(m.getBytes());
            target = new MapLocation(coords[0], coords[1]);
            System.out.println("target:" + target);
        }
        if (target != null) {
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            int dx = target.x - x;
            int dy = target.y - y;
            if (Math.abs(dx) > 4) {
                dx = 4 * (dx / Math.abs(dx));
            }
            if (Math.abs(dy) > 4) {
                dy = 4 * (dy / Math.abs(dy));
            }
            MapLocation t = new MapLocation(x + dx, y + dy);

            if (rc.canMove(dir)) {
                rc.move(dir);
            }

            UnitType type;
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                type = UnitType.LEVEL_ONE_MONEY_TOWER;
            } else {
                type = UnitType.LEVEL_ONE_PAINT_TOWER;
            }
            int x1 = rc.getLocation().x;
            int y1 = rc.getLocation().y;
            if (x1 < rc.getMapWidth() - 1 && y1 < rc.getMapHeight() - 1 && x1 > 0 && y1 > 0) {
                if (rc.getLocation().distanceSquaredTo(t.subtract(dir)) < 4 && rc.senseMapInfo(t.subtract(dir)).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(type, t)) {
                    rc.markTowerPattern(type, t);
                    System.out.println("Trying to build a tower at " + t);
                }
            }
            for (MapInfo patternTile : rc.senseNearbyMapInfos(t, 8)) {
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation()))
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                }
            }
            if (rc.canCompleteTowerPattern(type, t)) {
                rc.completeTowerPattern(type, t);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + t + "!");
                Tower.isSaving = false;
            }
        }
        if(target == null) {
            MapLocation[] nearbyRuins = rc.senseNearbyRuins(4);
            for(MapLocation l: nearbyRuins) {
                if(!knownTowers.contains(l)) {
                    target = l;
                    knownTowers.add(target);
                }
            }
            if(rc.canMarkResourcePattern(rc.getLocation())) rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, rc.getLocation());
        }
        if(moveCount == 0) {
            dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            previous = dir;
        }
        if(moveCount > moveLength) {
            moveCount = 0;
        }
        if (rc.canMove(previous)) {
            rc.move(previous);
        }

        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
            rc.attack(rc.getLocation());
        }
    }
    public static int getMapSize(RobotController rc) {
        return rc.getMapHeight() * rc.getMapWidth();
    }
}
