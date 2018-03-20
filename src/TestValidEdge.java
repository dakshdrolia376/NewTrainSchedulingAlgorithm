public class TestValidEdge {

    public boolean isExtraPlatform(TrainTime oldTrainArrStation2, TrainTime oldTrainDeptStation2, boolean isDirectLineAvailable,
                                   TrainTime nodeStart, TrainTime nodeEnd, int delayBwStation){
        boolean addedNodeEnd = false;
        int timeNodeStart = nodeStart.getValue();
        int timeNodeEnd = nodeEnd.getValue();
        if(timeNodeEnd<timeNodeStart){
            timeNodeEnd += 10080;
            addedNodeEnd = true;
        }
        int timeEarliestToReach = timeNodeStart + delayBwStation;

        //check platform requirement
        boolean addedOldTrain = false;
        if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
            System.out.println("Error in storing stoppage info info :");
        }

        int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
        int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

        if(oldTrainDeptStation2.day==0 && oldTrainArrStation2.day==6){
            addedOldTrain = true;
        }
        // else if(oldTrainDeptStation2.day==6 && oldTrainArrStation2.day==0){
        //     addedOldTrain = true;
        //     int temp = timeOldTrainArrStation2;
        //     timeOldTrainArrStation2 = timeOldTrainDeptStation2;
        //     timeOldTrainDeptStation2 = temp;
        // }
        // else{
        //     if(timeOldTrainDeptStation2<timeOldTrainArrStation2){
        //         int temp = timeOldTrainArrStation2;
        //         timeOldTrainArrStation2 = timeOldTrainDeptStation2;
        //         timeOldTrainDeptStation2 = temp;
        //     }
        // }

        if(!( isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
            //need to check availability of platform
            if(addedNodeEnd && addedOldTrain) {
                System.out.print("If 1 ");
                return true;
            }
            else if(addedOldTrain){
                System.out.print("If 2 ");
                return (timeNodeEnd >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2);
            }
            else if(addedNodeEnd){
                if(timeEarliestToReach>=10080){
                    System.out.print("If 3 1 ");
                    return !((timeNodeEnd - 10080) <= timeOldTrainArrStation2 || (timeEarliestToReach - 10080) >= timeOldTrainDeptStation2);
                }
                else {
                    System.out.print("If 3 2 ");
                    return ((timeNodeEnd - 10080) >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2);
                }
            }
            else{
                System.out.print("If 4 ");
                return !(timeNodeEnd <= timeOldTrainArrStation2 || timeEarliestToReach >= timeOldTrainDeptStation2);
            }
        }
        return false;
    }

    public boolean isExtraTrackInSameDirectionTrain(TrainTime oldTrainDeptStation1, TrainTime oldTrainArrStation2,
                                   TrainTime nodeStart, int delayBwStation, int minDelayBwTrains) {
        int timeNodeStart = nodeStart.getValue();
        int timeEarliestToReach = timeNodeStart + delayBwStation;
        int timeOldTrainDeptStation1 = oldTrainDeptStation1.getValue();
        int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
        boolean addedOldTrain = false;
        if(timeOldTrainArrStation2<timeOldTrainDeptStation1){
            timeOldTrainArrStation2+=10080;
            addedOldTrain=true;
        }

        if((timeOldTrainDeptStation1+minDelayBwTrains)>= timeNodeStart &&
                (timeOldTrainDeptStation1-minDelayBwTrains)<= timeNodeStart){
            System.out.print("Collision at s1 ");
            return true;
        }

        if((timeOldTrainArrStation2+minDelayBwTrains)>= timeEarliestToReach &&
                (timeOldTrainArrStation2-minDelayBwTrains)<= timeEarliestToReach){
            System.out.print("Collision at s2 ");
            return true;
        }

        if(timeEarliestToReach>=10080 && addedOldTrain){
            System.out.print("If 1 ");
            return (timeNodeStart < timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                    ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2));
        }
        else if(addedOldTrain){
            System.out.print("If 2 ");
            timeOldTrainArrStation2-=10080;
            return (timeEarliestToReach < timeOldTrainArrStation2) || (timeNodeStart > timeOldTrainDeptStation1);
        }
        else if(timeEarliestToReach>=10080){
            System.out.print("If 3 ");
            return (timeOldTrainArrStation2 < (timeEarliestToReach - 10080)) || (timeOldTrainDeptStation1 > timeNodeStart);
        }
        else{
            System.out.print("If 4 ");
            return (timeNodeStart < timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                    ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2));
        }
    }


    public boolean isExtraTrackInOppositeDirectionTrain(TrainTime oldTrainArrStation1, TrainTime oldTrainDeptStation2,
                                                    TrainTime nodeStart, int delayBwStation, int minDelayBwTrains) {
        int timeNodeStart = nodeStart.getValue();
        int timeEarliestToReach = timeNodeStart + delayBwStation;

        int timeOldTrainArrStation1 = oldTrainArrStation1.getValue();
        int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

        boolean addedOldTrain = false;
        if(timeOldTrainArrStation1<timeOldTrainDeptStation2){
            addedOldTrain=true;
            timeOldTrainArrStation1 += 10080;
        }

        if((timeOldTrainArrStation1+minDelayBwTrains)>= timeNodeStart &&
                (timeOldTrainArrStation1-minDelayBwTrains)<= timeNodeStart){
            System.out.print("Collision at s1 ");
            return true;
        }

        if((timeOldTrainDeptStation2+minDelayBwTrains)>= timeEarliestToReach &&
                (timeOldTrainDeptStation2-minDelayBwTrains)<= timeEarliestToReach){
            System.out.print("Collision at s2 ");
            return true;
        }

        if(timeEarliestToReach>=10080 && addedOldTrain){
            System.out.print("If 1 ");
            return (timeNodeStart < timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2);
        }
        else if(addedOldTrain){
            System.out.print("If 2 ");
            timeOldTrainArrStation1 -=10080;
            return (timeEarliestToReach > timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1);
        }
        else if(timeEarliestToReach>=10080){
            System.out.print("If 3 ");
            return ((timeEarliestToReach - 10080) > timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1);
        }
        else{
            System.out.print("If 4 ");
            return (timeNodeStart < timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2);
        }
    }

    public void testExtraTrackInOppositeDirectionTrain() {
        TrainTime oldTrainArrStation1;
        TrainTime oldTrainDeptStation2;
        TrainTime nodeStart;
        int delayBwStation;
        int minDelayBwTrains;

        //case1 collision at s1

        oldTrainDeptStation2 = new TrainTime(1,1,0);
        oldTrainArrStation1 = new TrainTime(1,1,12);
        nodeStart = new TrainTime(1,1,10);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,0);
        oldTrainArrStation1 = new TrainTime(1,1,12);
        nodeStart = new TrainTime(1,1,14);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case collision at s2

        oldTrainDeptStation2 = new TrainTime(1,1,20);
        oldTrainArrStation1 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,10);
        delayBwStation = 8;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,20);
        oldTrainArrStation1 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,10);
        delayBwStation = 12;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case added both
        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,10);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,10);
        delayBwStation = 90;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,50);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,50);
        delayBwStation = 90;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case added old only
        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,22,50);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,22,50);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(0,0,10);
        delayBwStation = 10;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(0,0,10);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,23,30);
        oldTrainArrStation1 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(5,22,50);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));


        //case added new only
        oldTrainDeptStation2 = new TrainTime(6,22,30);
        oldTrainArrStation1 = new TrainTime(6,22,50);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(6,22,30);
        oldTrainArrStation1 = new TrainTime(6,23,50);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(0,0,10);
        oldTrainArrStation1 = new TrainTime(0,0,20);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(0,0,10);
        oldTrainArrStation1 = new TrainTime(0,0,50);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,0,10);
        oldTrainArrStation1 = new TrainTime(1,0,50);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));


        //case not added both
        oldTrainDeptStation2 = new TrainTime(1,1,10);
        oldTrainArrStation1 = new TrainTime(1,2,10);
        nodeStart = new TrainTime(1,1,0);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,10);
        oldTrainArrStation1 = new TrainTime(1,2,10);
        nodeStart = new TrainTime(1,1,0);
        delayBwStation = 5;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,10);
        oldTrainArrStation1 = new TrainTime(1,2,10);
        nodeStart = new TrainTime(1,1,20);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,10);
        oldTrainArrStation1 = new TrainTime(1,2,10);
        nodeStart = new TrainTime(1,1,20);
        delayBwStation = 90;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation2 = new TrainTime(1,1,10);
        oldTrainArrStation1 = new TrainTime(1,2,10);
        nodeStart = new TrainTime(1,2,20);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInOppositeDirectionTrain(oldTrainArrStation1,oldTrainDeptStation2,nodeStart,delayBwStation,minDelayBwTrains));
    }

    public void testExtraTrackInSameDirectionTrain(){
        TrainTime oldTrainDeptStation1;
        TrainTime oldTrainArrStation2;
        TrainTime nodeStart;
        int delayBwStation;
        int minDelayBwTrains;

        //case collision at s1
        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,2,12);
        nodeStart = new TrainTime(1,1,13);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,2,12);
        nodeStart = new TrainTime(1,1,10);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case collision at s2
        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,2,12);
        nodeStart = new TrainTime(1,1,43);
        delayBwStation = 30;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,2,12);
        nodeStart = new TrainTime(1,1,43);
        delayBwStation = 28;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case both added
        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,0);
        delayBwStation = 62;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,0);
        delayBwStation = 100;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,20);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,20);
        delayBwStation = 42;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        // case only added old train

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,22,20);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,20);
        delayBwStation = 30;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,42);
        nodeStart = new TrainTime(0,0,2);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,12);
        oldTrainArrStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(0,0,20);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        //case added node only

        oldTrainDeptStation1 = new TrainTime(6,22,12);
        oldTrainArrStation2 = new TrainTime(6,22,42);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(6,23,42);
        oldTrainArrStation2 = new TrainTime(6,23,52);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(0,0,12);
        oldTrainArrStation2 = new TrainTime(0,0,22);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(0,0,12);
        oldTrainArrStation2 = new TrainTime(0,0,42);
        nodeStart = new TrainTime(6,23,30);
        delayBwStation = 60;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));


        //case not added both
        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,5);
        delayBwStation = 20;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,5);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,20);
        delayBwStation = 40;
        minDelayBwTrains = 3;
        System.out.println(!isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

        oldTrainDeptStation1 = new TrainTime(1,1,12);
        oldTrainArrStation2 = new TrainTime(1,1,32);
        nodeStart = new TrainTime(1,1,20);
        delayBwStation = 7;
        minDelayBwTrains = 3;
        System.out.println(isExtraTrackInSameDirectionTrain(oldTrainDeptStation1,oldTrainArrStation2,nodeStart,delayBwStation,minDelayBwTrains));

    }


    public void testPlatform(){
        TrainTime oldTrainArrStation2;
        TrainTime oldTrainDeptStation2;
        boolean isDirectLineAvailable;
        TrainTime nodeStart;
        TrainTime nodeEnd;
        int delayBwStation;

        //test case when added both
        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,21,12);
        nodeEnd = new TrainTime(0,1,15);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,40);
        nodeEnd = new TrainTime(0,0,10);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,40);
        nodeEnd = new TrainTime(0,0,20);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,22,40);
        nodeEnd = new TrainTime(0,0,10);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));


        // test case when added only old train:
        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,22,40);
        nodeEnd = new TrainTime(6,23,8);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,22,40);
        nodeEnd = new TrainTime(6,23,40);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,0,12);
        nodeStart = new TrainTime(6,23,30);
        nodeEnd = new TrainTime(6,23,40);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,1,12);
        nodeStart = new TrainTime(0,0,40);
        nodeEnd = new TrainTime(0,1,10);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,1,12);
        nodeStart = new TrainTime(0,0,40);
        nodeEnd = new TrainTime(0,1,20);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(0,1,12);
        nodeStart = new TrainTime(0,1,40);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        //test case when added new only
        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(6,23,55);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,54);
        oldTrainDeptStation2 = new TrainTime(6,23,58);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,12);
        oldTrainDeptStation2 = new TrainTime(6,23,20);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,1);
        oldTrainDeptStation2 = new TrainTime(0,0,5);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,1);
        oldTrainDeptStation2 = new TrainTime(0,0,50);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,1);
        oldTrainDeptStation2 = new TrainTime(0,2,5);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,20);
        oldTrainDeptStation2 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,20);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,20);
        oldTrainDeptStation2 = new TrainTime(0,3,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 20;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,10);
        oldTrainDeptStation2 = new TrainTime(6,23,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,10);
        oldTrainDeptStation2 = new TrainTime(6,23,55);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(6,23,53);
        oldTrainDeptStation2 = new TrainTime(6,23,58);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,10);
        oldTrainDeptStation2 = new TrainTime(0,0,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,0,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(6,23,50);
        nodeEnd = new TrainTime(0,1,50);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        //case when not added in any
        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,1,50);
        nodeEnd = new TrainTime(0,1,55);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,1,50);
        nodeEnd = new TrainTime(0,2,20);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,1,50);
        nodeEnd = new TrainTime(0,2,55);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,2,20);
        nodeEnd = new TrainTime(0,2,25);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,2,20);
        nodeEnd = new TrainTime(0,2,55);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));

        oldTrainArrStation2 = new TrainTime(0,2,10);
        oldTrainDeptStation2 = new TrainTime(0,2,30);
        nodeStart = new TrainTime(0,2,40);
        nodeEnd = new TrainTime(0,2,45);
        isDirectLineAvailable = true;
        delayBwStation = 0;
        System.out.println(!isExtraPlatform(oldTrainArrStation2,oldTrainDeptStation2,isDirectLineAvailable,nodeStart, nodeEnd, delayBwStation));
    }

    public static void main(String... S){
        TestValidEdge testValidEdge = new TestValidEdge();
        testValidEdge.testPlatform();
        testValidEdge.testExtraTrackInSameDirectionTrain();
        testValidEdge.testExtraTrackInOppositeDirectionTrain();
    }
}
