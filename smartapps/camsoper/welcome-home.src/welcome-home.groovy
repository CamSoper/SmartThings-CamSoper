/**
 *  Welcome Home
 *
 *  Copyright 2017 Cam Soper
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

/* Changelog

9/14/2007 - Added a changelog. Deleted a comment.

*/

definition(
    name: "Welcome Home",
    namespace: "CamSoper",
    author: "Cam Soper",
    description: "Welcomes presence devices when an entry opens/closes.",
    category: "Family",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Who are we greeting?") {
        input "greetingList", "capability.presenceSensor", multiple: true, required: true
    }
    section("Which doors do they enter?") {
        input "doorList", "capability.contactSensor", multiple: true, required: true
    }
    section("Where are we speaking?") {
        input "mySpeaker", "capability.musicPlayer", required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    log.debug("Initializing")
    atomicState.newArrivals = []
    subscribe(greetingList, "presence.present", "presenceHandler")
    subscribe(doorList, "contact.open", "contactHandler")
}

def presenceHandler(evt) {
    log.debug("There are currently $state.newArrivals.size items in the list.")

    def theDevice = evt.device
    def newArrivals = atomicState.newArrivals
    def personsName = ""
    if(theDevice.label == null || theDevice.label == "") {
        personsName = theDevice.name
    }
    else {
        personsName = theDevice.label
    }

    log.debug("$personsName is home.")
    if(!newArrivals.contains(personsName)) {
        log.debug("Adding unique item.")
        newArrivals.add(personsName)
        atomicState.newArrivals = newArrivals
    }

    log.debug("There are now $newArrivals.size items in the list.")
}

def contactHandler(evt) {
    log.debug("The $evt.device.label sensor is open.")
    def newArrivals = atomicState.newArrivals
    def arrivalCount = newArrivals.size
    if(arrivalCount > 0){
    	log.debug("There are people to welcome.")
        def welcomeText = "Welcome home, "
        if(arrivalCount > 1){
            for(int i = 0; i < arrivalCount; i++){
                welcomeText += newArrivals[i]
                if(i < arrivalCount - 2 && arrivalCount > 2){
                    welcomeText += ", " // Three or more and this is NOT the next-to-last one, add a comma
                }
                else if(i == arrivalCount - 2 && arrivalCount > 2){
                    welcomeText += ", and "  // Three or more and this is the next-to-last one, Oxford comma before "and"
                }
                else if(i == arrivalCount - 2 && arrivalCount == 2){
                    welcomeText += " and "  // Two items in list, no commas
                }
            }
        }
        welcomeText += "!"
        log.debug("Speaking $welcomeText")
        mySpeaker.speak(welcomeText)
        atomicState.newArrivals = []
    }
}
