// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.*;

public final class FindMeetingQuery {
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> meetingAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Collection<String> totalAttendees = new ArrayList<>();
    totalAttendees.addAll(meetingAttendees);
    totalAttendees.addAll(optionalAttendees);
    List<TimeRange> eventTimeRangesComp = new ArrayList<>();
    List<TimeRange> eventTimeRangesTotal = new ArrayList<>();
    List<TimeRange> possibleTimeRanges = new ArrayList<>();
    int duration = (int) request.getDuration();
    //If no meeting attendees
    if (totalAttendees.size() == 0) { 
        possibleTimeRanges.add(TimeRange.WHOLE_DAY);
        return possibleTimeRanges;
    }
    //If duration > than whole day
    if (duration > TimeRange.WHOLE_DAY.duration()) {
        return possibleTimeRanges;
    }
    events.forEach((event) -> {
        Set<String> eventAttendees = event.getAttendees(); //unmodifiable collection
        Set<String> eventAttendeesCopy = new HashSet<>();
        Set<String> eventAttendeesCopyTotal = new HashSet<>();
        eventAttendeesCopy.addAll(eventAttendees);
        eventAttendeesCopy.retainAll(meetingAttendees);
        //Overlap exists between event and compulsory attendees
        if (eventAttendeesCopy.size() > 0) {
            eventTimeRangesComp.add(event.getWhen());
        }
        eventAttendeesCopyTotal.addAll(eventAttendees);
        eventAttendeesCopyTotal.retainAll(totalAttendees);
        //Overlap exists between event and total attendees
        if (eventAttendeesCopyTotal.size() > 0) {
            eventTimeRangesTotal.add(event.getWhen());
        }
    });
    //If no events 
    if (eventTimeRangesTotal.size() == 0) {
        possibleTimeRanges.add(TimeRange.WHOLE_DAY);
        return possibleTimeRanges;
    }
    Collections.sort(eventTimeRangesComp, TimeRange.ORDER_BY_START);
    Collections.sort(eventTimeRangesTotal, TimeRange.ORDER_BY_START);
    List<List<TimeRange>> bothEventTimeRanges = new ArrayList<List<TimeRange>>();
    List<List<TimeRange>> bothPossibleTimeRanges = new ArrayList<List<TimeRange>>();
    bothEventTimeRanges.add(eventTimeRangesComp);
    bothEventTimeRanges.add(eventTimeRangesTotal);
    List<TimeRange> eventTimeRangesFinal = new ArrayList<>();
    int start;
    //Consolidates all the event timings, combining overlaps
    for (List<TimeRange> eventTimeRanges: bothEventTimeRanges) {
        if (eventTimeRanges.size() > 0) {
            eventTimeRangesFinal.clear();
            possibleTimeRanges.clear();
            start = TimeRange.START_OF_DAY;
            if (eventTimeRanges.size() > 1) {
                int prevStart = eventTimeRanges.get(0).start();
                int prevEnd = eventTimeRanges.get(0).end();
                for (int i = 1; i < eventTimeRanges.size(); i++) {
                    int curStart = eventTimeRanges.get(i).start();
                    int curEnd = eventTimeRanges.get(i).end();
                    if (curStart < prevEnd) {
                        if (curEnd > prevEnd) {
                            prevEnd = curEnd;
                        }
                    }
                    else {
                        eventTimeRangesFinal.add(TimeRange.fromStartEnd(prevStart, prevEnd, false));
                        prevStart = curStart;
                        prevEnd = curEnd;
                    }
                }
                eventTimeRangesFinal.add(TimeRange.fromStartEnd(prevStart, prevEnd, false));
            }
            else {
                eventTimeRangesFinal.add(eventTimeRanges.get(0));
            }
            int k = 0;
            while (start < TimeRange.END_OF_DAY && k < eventTimeRangesFinal.size()) {
                TimeRange timeRange= eventTimeRangesFinal.get(k);
                if (timeRange.start()- start >= duration)  {
                    possibleTimeRanges.add(TimeRange.fromStartEnd(start, timeRange.start(), false));
                }
                start = timeRange.end();
                k++;
            }
            if (start != TimeRange.END_OF_DAY+1) {
                possibleTimeRanges.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
            }
            List<TimeRange> temp = new ArrayList<>();
            temp.addAll(possibleTimeRanges);
            bothPossibleTimeRanges.add(temp);
        }
        else {
            bothPossibleTimeRanges.add(new ArrayList<>());
        }
    }
    if (bothPossibleTimeRanges.get(1).size() > 0) {
        return bothPossibleTimeRanges.get(1);
    }
    else {
        return bothPossibleTimeRanges.get(0);
    }
  }
}
