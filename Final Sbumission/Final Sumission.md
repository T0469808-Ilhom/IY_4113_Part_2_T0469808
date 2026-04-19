# IY4113 Applied Software Engineering — Final Report Part 2

| Detail            | Information                                               |
| ----------------- | --------------------------------------------------------- |
| Module Code       | IY4113                                                    |
| Group             | B                                                         |
| Module Title      | Practical Assignment Part 2: Java Fundamentals            |
| Assessment Type   | Report                                                    |
| Module Tutor      | Jonathan Shore                                            |
| Student ID        | P0469808                                                  |
| Assessment Window | Final submission: 12/04/2026 - 19/04/2026                 |
| GitHub            | https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808 |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*

- [x] Where I have used AI, I have cited and referenced appropriately.

## Introduction

Object-oriented programming has become one of the most widely used approaches in modern software development, allowing developers to build complex systems from small, reusable components that each have a clear responsibility (Barnes and Kolling, 2016). This report documents the design, development and evaluation of CityRide Lite Part 2, a Java console application built as part of the IY4113 Applied Software Engineering module. The program is an extension of Part 1 and expands the original fare calculation system into a more complete and realistic transport companion application.

The purpose of CityRide Lite Part 2 is to allow riders to manage their daily travel and enable administrators to maintain the fare configuration system. The program supports two distinct roles. A rider can create or load a personal profile, add and manage journeys for the active day, calculate fares with discounts and daily caps applied, view running totals, generate end-of-day summaries, and export reports in both CSV and human-readable text formats. An administrator can log in through a password-protected menu to view and manage the system configuration, including base fares, passenger discounts, daily caps and peak time windows.

Compared to Part 1, this version introduces persistent file storage using JSON for profiles and configuration, and CSV for journey data and reports. This means a rider's data is saved between sessions rather than being lost when the program closes. The system is designed to follow object-oriented principles throughout, with each class having a single clear responsibility, and all behaviour encapsulated within classes in accordance with the NTIC Guide to Good Programming.

The report covers the analysis of requirements and IPO tables, algorithm designs in the form of flowcharts, black box testing of input validation methods, an evaluation of the completed system, the full code listing and the milestone documentation as appendices.

## Analysis and Design

### Functional Requirements

The system has two roles: Rider and Admin. Each role has its own menu and can only access its own features. The admin menu requires a password to open.

When the program starts, it loads the configuration from a JSON file. If the file is missing, the system uses built-in default values so it can still run normally.

A rider can create a new profile or load a saved one. The profile stores the rider's name, passenger type and default payment option. Profiles are saved and loaded as JSON files. When a rider exits, the system asks whether to save the profile and journeys.

Riders can add, edit and delete journeys for the active day. Each journey stores a unique ID, date and time, from zone, to zone, time band, passenger type, zones crossed, base fare, discount applied, discounted fare and charged fare. The time band is set automatically based on the journey time. If a journey is deleted or edited, the system recalculates the fares for all remaining journeys that day. If a journey ID does not exist, the system shows an error.

Fares are calculated in three steps: find the base fare for the zone pair and time band, apply the passenger discount, then apply the daily cap. The charged fare never goes above the cap. Riders can see the cost of each journey, the daily running total, and whether the cap has been reached.

Riders can view an end-of-day summary that includes the total journeys, total cost, average cost per journey, the most expensive journey, savings from the cap, peak and off-peak counts, and zone counts. The summary can be exported as a text file or CSV file. Riders can also import and export journey data as CSV.

Admins can view the current configuration and manage base fares, discounts, daily caps and peak windows. Each category has add, update and delete options. Delete restores the default value. All changes are validated before saving.

### Non-Functional Requirements

Every menu shows numbered options. Every prompt tells the user what format to use and gives an example.

All input is validated before it is used. If the input is wrong, the system shows a clear error message and does not save anything.

The system uses JSON for profiles and configuration, and CSV for journeys and reports. Only core Java is used — no external libraries.

The code follows the NTIC Guide to Good Programming. Each class does one thing, fields are private, and naming conventions are applied consistently throughout.

### System Constraints

**File format rules** — profiles and configuration must be stored as JSON. Journeys and reports must be stored as CSV or TXT.

**Journey data rules** — every journey must contain all required fields: unique ID, date and time, from zone, to zone, time band, passenger type, zones crossed, base fare, discount applied, discounted fare and charged fare. Incomplete journey records are not accepted.

**Fare rules** — the system must use the values defined in CityRideDataset for base fares, discounts and daily caps. The daily total charged to a rider can never exceed the cap for their passenger type.

**Before saving** — no data is saved if it fails validation. The system must show a clear error message and wait for a valid input.

**Safe startup** — if the configuration file is missing when the program starts, the system must load default values and continue running. It must not crash.

**Recalculation on change** — any time a journey is added, edited or deleted, the system must recalculate the fares and cap for all journeys on that day.

**Admin security** — the admin menu is only accessible with the correct password. Riders cannot access admin functions.

**Save on exit** — when a rider exits, the system must ask whether to save the profile and journeys before closing.

**No external libraries** — the program uses only core Java. No third-party libraries are allowed.

### System Interactions

When the program launches, `ConfigManager` attempts to load `config.json` via `JsonFileHandler`. If the file is missing, `ConfigManager` automatically generates safe defaults from `CityRideDataset` so the system is always in a usable state before any menu is shown.

When a rider adds a journey, `RiderMenu` collects the time, zones and active date from the user, then passes them to `ConfigManager` which automatically determines the time band from the journey time. `JourneyManager` then calls `FareCalculator` to apply the passenger discount and daily cap, creates a `Journey` object with all calculated fields, and stores it in memory. Because all journeys are scoped to the `activeDate`, only that day's journeys are affected by cap recalculation.

When a journey is deleted or edited, `JourneyManager` immediately recalculates the charged fares for all remaining journeys on that day, so the cap is always correctly applied across the whole day, not just the modified journey.

When the rider accesses Summary and Reports, `SummaryReport` reads journeys directly from `JourneyManager` for the active date and calculates all summary values in memory. `ReportExporter` then writes the output to file via `CsvFileHandler` for CSV reports and using `BufferedWriter` directly for text summaries, with the filename automatically built from the rider's profile ID, name and date.

On the admin side, all config changes go through `ConfigManager` which updates `SystemConfig` in memory and immediately persists the change to `config.json` via `JsonFileHandler`, ensuring the rider session always uses the latest values.

#### IPO Table:

### IPO Table

Inputs are labelled as **User input**, **System**, or **File input**.

| Feature                         | Input                                                                                       | Process                                                                                                                                                                                                                     | Output                                                                                     |
| ------------------------------- | ------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| Start program                   | System: program launch. File: config.json                                                   | The system loads the configuration file on startup. If the file is not found, safe default values are used instead.                                                                                                         | Main menu is displayed and configuration is ready                                          |
| Select role                     | User: choice (0-2)                                                                          | The system checks the choice and opens the right menu for that role.                                                                                                                                                        | Rider menu, Admin login, or program exits                                                  |
| Create profile                  | User: name, passenger type, payment option                                                  | The system validates the details, generates a unique profile ID and saves the profile as a JSON file.                                                                                                                       | Profile is created and the ID is shown to the rider                                        |
| Load profile                    | User: profile ID (e.g. R1). File: R[id].json, R[id]_journeys.csv                            | All saved profile IDs are listed first. The system reads the JSON file and loads any saved journeys from CSV.                                                                                                               | Profile and journeys are loaded, or an error is shown if the ID is not found               |
| Save profile                    | User: Y/N. System: current profile                                                          | If the rider confirms, the profile data is written to the JSON file.                                                                                                                                                        | Profile is saved, or the rider exits without saving                                        |
| Set active date                 | User: date (DD-MM-YYYY, e.g. 22-04-2026)                                                    | The system validates the date format and sets it as the active date for the session.                                                                                                                                        | Active date is confirmed and shown                                                         |
| Add journey                     | User: time (HH:mm), from zone (1-5), to zone (1-5). System: active date, profile, config    | The time band is determined automatically from the journey time. The system gets the base fare, applies the passenger discount, checks the running total against the daily cap, assigns a unique ID and stores the journey. | Journey is added, or an error is shown if no fare exists for that zone pair                |
| Edit journey                    | User: journey ID, new time, new from zone, new to zone. System: active day journeys, config | The system finds the journey by ID and updates the fields. The time band is determined automatically from the new time. Fares and the daily cap are recalculated for all journeys that day.                                 | Journey is updated, or an error is shown if the ID is not found                            |
| Delete journey                  | User: journey ID, Y/N confirmation. System: active day journeys, config                     | The system finds the journey by ID and asks for confirmation. If confirmed, the journey is removed and fares are recalculated for all remaining journeys that day.                                                          | Journey is deleted and fares are recalculated, or the deletion is cancelled                |
| List journeys                   | System: active day journeys                                                                 | The system retrieves all journeys for the active date and displays each one with full details including whether the cap was applied.                                                                                        | All journeys are displayed, or a message is shown if none exist                            |
| Calculate / View running totals | System: active day journeys, config, passenger type                                         | The system adds up all charged fares for the active day and compares the total to the daily cap for that passenger type.                                                                                                    | Total charged, daily cap amount, and cap reached status are shown                          |
| Import journeys                 | File: R[id]_journeys.csv                                                                    | The system opens the CSV file, skips the header row, parses each row and loads valid journeys into memory. Invalid rows are skipped with a warning.                                                                         | Journeys are loaded and the count is shown, or a message is shown if the file is not found |
| Export journeys                 | System: active day journeys, profile ID                                                     | The system formats the active day journeys as CSV rows and writes them to the rider's journey file.                                                                                                                         | CSV file is saved, or a message is shown if there are no journeys to export                |
| Show daily summary              | System: active day journeys                                                                 | The system calculates the total journeys, total cost, average cost, most expensive journey, cap savings, cap status, peak and off-peak counts, and zone pair counts.                                                        | The full daily summary is displayed on screen                                              |
| Export summary as text          | System: summary data, rider name, active date                                               | The system builds the summary as a formatted text string and writes it to a .txt file named using the profile ID, rider name and date.                                                                                      | Text file is saved and the filename is shown, or an error is displayed                     |
| Export summary as CSV           | System: active day journeys, rider name, active date                                        | The system formats each journey and the daily totals as CSV rows and writes them to a .csv file named using the profile ID, rider name and date.                                                                            | CSV file is saved and the filename is shown, or an error is displayed                      |
| Admin login                     | User: password string                                                                       | The system compares the entered password against the stored admin password and grants access only if it matches.                                                                                                            | Admin menu is shown, or access is denied                                                   |
| View config                     | System: current config                                                                      | The system reads all base fares, discounts, daily caps and the peak window from memory and displays them.                                                                                                                   | Full configuration is displayed                                                            |
| Add / Update base fare          | User: from zone, to zone, time band, new fare (GBP, e.g. 3.50)                              | The system validates all inputs, updates the fare in the active configuration and saves the change to config.json.                                                                                                          | Fare is updated and saved, or a validation error is shown                                  |
| Delete base fare                | User: from zone, to zone, time band                                                         | The system retrieves the original default fare from CityRideDataset, restores it in the configuration and saves to config.json.                                                                                             | Fare is restored to its default value                                                      |
| Add / Update discount           | User: passenger type, discount rate (0.00-1.00, e.g. 0.25)                                  | The system validates the rate is between 0.00 and 1.00, updates the discount in the configuration and saves to config.json.                                                                                                 | Discount is updated and saved, or a validation error is shown                              |
| Delete discount                 | User: passenger type                                                                        | The system retrieves the original default discount from CityRideDataset, restores it in the configuration and saves to config.json.                                                                                         | Discount is restored to its default value                                                  |
| Add / Update daily cap          | User: passenger type, new cap (GBP, e.g. 8.00)                                              | The system validates the cap is greater than zero, updates it in the configuration and saves to config.json.                                                                                                                | Cap is updated and saved, or a validation error is shown                                   |
| Delete daily cap                | User: passenger type                                                                        | The system retrieves the original default cap from CityRideDataset, restores it in the configuration and saves to config.json.                                                                                              | Cap is restored to its default value                                                       |
| Add / Update peak window        | User: peak start (HH:mm, e.g. 07:00), peak end (HH:mm, e.g. 09:00)                          | The system validates both times and checks that the start is before the end. If valid, the peak window is updated in the configuration and saved to config.json.                                                            | Peak window is updated and saved, or a validation error is shown                           |
| Delete peak window              | System: default values (07:00-09:00)                                                        | The system restores the default peak window of 07:00 to 09:00 in the configuration and saves to config.json.                                                                                                                | Peak window is restored to 07:00-09:00                                                     |
| Save on exit (profile)          | User: Y/N. System: current profile                                                          | The system asks whether to save the profile. If confirmed, the profile is written to the JSON file.                                                                                                                         | Profile is saved, or the rider exits without saving                                        |
| Save on exit (journeys)         | User: Y/N. System: active day journeys                                                      | The system asks whether to save the active day journeys. If confirmed, they are written to the rider's CSV file.                                                                                                            | Journeys are saved, or the rider exits without saving                                      |
| Change active day               | User: new date (DD-MM-YYYY, e.g. 23-04-2026)                                                | The system validates the new date format and updates the active date for the session. All journey operations from this point are scoped to the new date.                                                                    | Active date is updated and confirmed                                                       |

#### Flowcharts:

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%201.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%202.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%203.jpg) 

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%204.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%205.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%206.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%207.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%208.jpg)

![]()

![]() 

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%209.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%2010.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%2011.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%2012.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%2013.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Final%20Sbumission/Final%20flowcahrts/Flowchart%2014.jpg)

## Testing

All tests target `InputHelper` methods — the single point of all user input validation in the system. Total: 110 tests. All tests passed.

| No. | Input                 | Test Description               | Test Data        | Expected Output                    | Actual Output                                 | Pass/Fail |
| --- | --------------------- | ------------------------------ | ---------------- | ---------------------------------- | --------------------------------------------- | --------- |
| 1   | Main menu             | Valid choice Rider             | 1                | Accepted, returns 1                | Returned 1 (Rider)                            | Pass      |
| 2   | Main menu             | Valid choice Admin             | 2                | Accepted, returns 2                | Returned 2 (Admin)                            | Pass      |
| 3   | Main menu             | Valid choice Exit              | 0                | Accepted, returns 0                | Returned 0 (Exit)                             | Pass      |
| 4   | Main menu             | Out of range too high          | 3                | Error shown and re-prompted        | Error: please enter a number from 0 to 2      | Pass      |
| 5   | Main menu             | Out of range too low           | -1               | Error shown and re-prompted        | Error: please enter a number from 0 to 2      | Pass      |
| 6   | Main menu             | Letter input                   | abc              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 7   | Main menu             | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 8   | Main menu             | Decimal number                 | 1.5              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 9   | Journey menu          | Valid choice Add               | 1                | Accepted, returns 1                | Returned 1 (Add journey)                      | Pass      |
| 10  | Journey menu          | Valid choice Exit              | 0                | Accepted, returns 0                | Returned 0 (Exit)                             | Pass      |
| 11  | Journey menu          | Out of range too high          | 8                | Error shown and re-prompted        | Error: please enter a number from 0 to 7      | Pass      |
| 12  | Journey menu          | Out of range too low           | -1               | Error shown and re-prompted        | Error: please enter a number from 0 to 7      | Pass      |
| 13  | Journey menu          | Letter input                   | abc              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 14  | Journey menu          | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 15  | Add journey from zone | Valid zone min boundary        | 1                | Accepted, returns 1                | Returned 1 (zone 1)                           | Pass      |
| 16  | Add journey from zone | Valid zone max boundary        | 5                | Accepted, returns 5                | Returned 5 (zone 5)                           | Pass      |
| 17  | Add journey from zone | Valid zone mid                 | 3                | Accepted, returns 3                | Returned 3 (zone 3)                           | Pass      |
| 18  | Add journey from zone | Out of range too high          | 6                | Error shown and re-prompted        | Error: please enter a number from 1 to 5      | Pass      |
| 19  | Add journey from zone | Out of range too low           | 0                | Error shown and re-prompted        | Error: please enter a number from 1 to 5      | Pass      |
| 20  | Add journey from zone | Negative number                | -1               | Error shown and re-prompted        | Error: please enter a number from 1 to 5      | Pass      |
| 21  | Add journey from zone | Letter input                   | abc              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 22  | Add journey from zone | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 23  | Add journey from zone | Decimal number                 | 2.5              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 24  | Admin menu            | Valid choice View config       | 1                | Accepted, returns 1                | Returned 1 (View config)                      | Pass      |
| 25  | Admin menu            | Valid choice Back              | 0                | Accepted, returns 0                | Returned 0 (Back)                             | Pass      |
| 26  | Admin menu            | Out of range too high          | 6                | Error shown and re-prompted        | Error: please enter a number from 0 to 5      | Pass      |
| 27  | Admin menu            | Letter input                   | abc              | Error: please enter a whole number | Error: please enter a whole number            | Pass      |
| 28  | Admin menu            | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 29  | Add journey time      | Valid time                     | 08:30            | Time accepted                      | Accepted 08:30                                | Pass      |
| 30  | Add journey time      | Peak boundary start            | 07:00            | Time accepted                      | Accepted 07:00                                | Pass      |
| 31  | Add journey time      | Off-peak time                  | 11:00            | Time accepted                      | Accepted 11:00                                | Pass      |
| 32  | Add journey time      | Midnight boundary              | 00:00            | Time accepted                      | Accepted 00:00                                | Pass      |
| 33  | Add journey time      | End of day boundary            | 23:59            | Time accepted                      | Accepted 23:59                                | Pass      |
| 34  | Add journey time      | Wrong format single digit hour | 8:30             | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 35  | Add journey time      | Invalid time hour too high     | 25:00            | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 36  | Add journey time      | Invalid time minute too high   | 08:60            | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 37  | Add journey time      | Letter input                   | abc              | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 38  | Add journey time      | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 39  | Add journey time      | Wrong separator                | 08.30            | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 40  | Set active date       | Valid date                     | 22-04-2026       | Date accepted                      | Accepted 22-04-2026                           | Pass      |
| 41  | Set active date       | Valid date different           | 01-01-2026       | Date accepted                      | Accepted 01-01-2026                           | Pass      |
| 42  | Set active date       | Wrong format slashes           | 22/04/2026       | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 43  | Set active date       | Wrong format no leading zero   | 2-4-2026         | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 44  | Set active date       | Invalid day too high           | 32-04-2026       | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 45  | Set active date       | Invalid month too high         | 22-13-2026       | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 46  | Set active date       | Letter input                   | abc              | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 47  | Set active date       | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 48  | Set active date       | Wrong order year first         | 2026-04-22       | Error: use DD-MM-YYYY              | Error: invalid date, use DD-MM-YYYY           | Pass      |
| 49  | Delete confirm        | Yes uppercase                  | Y                | Returns true, confirmed            | Accepted Y, returned true                     | Pass      |
| 50  | Delete confirm        | Yes lowercase                  | y                | Returns true, confirmed            | Accepted y, returned true                     | Pass      |
| 51  | Delete confirm        | Yes full word                  | YES              | Returns true, confirmed            | Accepted YES, returned true                   | Pass      |
| 52  | Delete confirm        | No uppercase                   | N                | Returns false, cancelled           | Accepted N, returned false                    | Pass      |
| 53  | Delete confirm        | No lowercase                   | n                | Returns false, cancelled           | Accepted n, returned false                    | Pass      |
| 54  | Delete confirm        | No full word                   | NO               | Returns false, cancelled           | Accepted NO, returned false                   | Pass      |
| 55  | Delete confirm        | Invalid input                  | maybe            | Error: enter Y or N                | Error: please enter Y or N                    | Pass      |
| 56  | Delete confirm        | Number input                   | 1                | Error: enter Y or N                | Error: please enter Y or N                    | Pass      |
| 57  | Delete confirm        | Empty input                    | (empty)          | Error: enter Y or N                | Error: please enter Y or N                    | Pass      |
| 58  | Delete confirm        | Special characters             | @#!              | Error: enter Y or N                | Error: please enter Y or N                    | Pass      |
| 59  | Create profile name   | Standard name                  | Ilhom            | Name accepted and stored           | Accepted and stored Ilhom                     | Pass      |
| 60  | Create profile name   | Name with space                | Ali Khan         | Full name accepted and stored      | Accepted and stored Ali Khan                  | Pass      |
| 61  | Create profile name   | Numbers only                   | 12345            | Accepted as valid name             | Accepted and stored 12345                     | Pass      |
| 62  | Create profile name   | Special characters             | @#!              | Accepted as valid name             | Accepted and stored @#!                       | Pass      |
| 63  | Create profile name   | Single character               | A                | Accepted as valid name             | Accepted and stored A                         | Pass      |
| 64  | Create profile name   | Very long name                 | Thisverylongname | Accepted as valid name             | Accepted and stored Thisverylongname          | Pass      |
| 65  | Create profile name   | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 66  | Passenger type        | Valid ADULT                    | ADULT            | Type set to ADULT                  | Accepted and set to ADULT                     | Pass      |
| 67  | Passenger type        | Valid STUDENT                  | STUDENT          | Type set to STUDENT                | Accepted and set to STUDENT                   | Pass      |
| 68  | Passenger type        | Valid CHILD                    | CHILD            | Type set to CHILD                  | Accepted and set to CHILD                     | Pass      |
| 69  | Passenger type        | Valid SENIOR_CITIZEN           | SENIOR_CITIZEN   | Type set to SENIOR_CITIZEN         | Accepted and set to SENIOR_CITIZEN            | Pass      |
| 70  | Passenger type        | Valid lowercase adult          | adult            | Accepted, converted to ADULT       | Converted and set to ADULT                    | Pass      |
| 71  | Passenger type        | Valid SENIOR shorthand         | SENIOR           | Accepted as SENIOR_CITIZEN         | Converted and set to SENIOR_CITIZEN           | Pass      |
| 72  | Passenger type        | Invalid type                   | WORKER           | Error: invalid passenger type      | Error: invalid passenger type                 | Pass      |
| 73  | Passenger type        | Empty input                    | (empty)          | Error: invalid passenger type      | Error: invalid passenger type                 | Pass      |
| 74  | Passenger type        | Number input                   | 123              | Error: invalid passenger type      | Error: invalid passenger type                 | Pass      |
| 75  | Payment option        | Valid CARD                     | CARD             | Option set to CARD                 | Accepted and set to CARD                      | Pass      |
| 76  | Payment option        | Valid CASH                     | CASH             | Option set to CASH                 | Accepted and set to CASH                      | Pass      |
| 77  | Payment option        | Valid lowercase card           | card             | Accepted as CARD                   | Converted and set to CARD                     | Pass      |
| 78  | Payment option        | Invalid PAYPAL                 | PAYPAL           | Error: enter CARD or CASH          | Error: invalid payment option                 | Pass      |
| 79  | Payment option        | Empty input                    | (empty)          | Error: enter CARD or CASH          | Error: invalid payment option                 | Pass      |
| 80  | Payment option        | Number input                   | 1                | Error: enter CARD or CASH          | Error: invalid payment option                 | Pass      |
| 81  | Time band             | Valid PEAK                     | PEAK             | Band set to PEAK                   | Accepted and set to PEAK                      | Pass      |
| 82  | Time band             | Valid shorthand P              | P                | Accepted as PEAK                   | Converted and set to PEAK                     | Pass      |
| 83  | Time band             | Valid OFF-PEAK                 | OFF-PEAK         | Band set to OFF_PEAK               | Accepted and set to OFF_PEAK                  | Pass      |
| 84  | Time band             | Valid shorthand O              | O                | Accepted as OFF_PEAK               | Converted and set to OFF_PEAK                 | Pass      |
| 85  | Time band             | Valid OFF_PEAK underscore      | OFF_PEAK         | Accepted as OFF_PEAK               | Accepted and set to OFF_PEAK                  | Pass      |
| 86  | Time band             | Valid lowercase peak           | peak             | Accepted as PEAK                   | Converted and set to PEAK                     | Pass      |
| 87  | Time band             | Invalid MORNING                | MORNING          | Error: enter PEAK or OFF-PEAK      | Error: invalid time band                      | Pass      |
| 88  | Time band             | Empty input                    | (empty)          | Error: enter PEAK or OFF-PEAK      | Error: invalid time band                      | Pass      |
| 89  | Time band             | Number input                   | 1                | Error: enter PEAK or OFF-PEAK      | Error: invalid time band                      | Pass      |
| 90  | Fare amount           | Valid fare                     | 3.50             | Accepted                           | Accepted GBP 3.50                             | Pass      |
| 91  | Fare amount           | Valid whole number             | 5                | Accepted                           | Accepted GBP 5                                | Pass      |
| 92  | Fare amount           | Valid small value              | 0.01             | Accepted                           | Accepted GBP 0.01                             | Pass      |
| 93  | Fare amount           | Zero value                     | 0                | Error: must be greater than zero   | Error: value must be greater than zero        | Pass      |
| 94  | Fare amount           | Negative value                 | -1.00            | Error: must be greater than zero   | Error: value must be greater than zero        | Pass      |
| 95  | Fare amount           | Letter input                   | abc              | Error: enter a valid number        | Error: please enter a valid number            | Pass      |
| 96  | Fare amount           | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 97  | Fare amount           | Special characters             | @#!              | Error: enter a valid number        | Error: please enter a valid number            | Pass      |
| 98  | Discount rate         | Valid 0.25                     | 0.25             | Accepted                           | Accepted 0.25 (25%)                           | Pass      |
| 99  | Discount rate         | Boundary low 0.00              | 0.00             | Accepted                           | Accepted 0.00 (0%)                            | Pass      |
| 100 | Discount rate         | Boundary high 1.00             | 1.00             | Accepted                           | Accepted 1.00 (100%)                          | Pass      |
| 101 | Discount rate         | Out of range 1.01              | 1.01             | Error: between 0.00 and 1.00       | Error: discount must be between 0.00 and 1.00 | Pass      |
| 102 | Discount rate         | Negative value                 | -0.10            | Error: between 0.00 and 1.00       | Error: discount must be between 0.00 and 1.00 | Pass      |
| 103 | Discount rate         | Letter input                   | abc              | Error: enter a valid decimal       | Error: please enter a valid decimal           | Pass      |
| 104 | Discount rate         | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass      |
| 105 | Peak window time      | Valid start time               | 07:00            | Time accepted                      | Accepted 07:00 as start                       | Pass      |
| 106 | Peak window time      | Valid end time                 | 09:00            | Time accepted                      | Accepted 09:00 as end                         | Pass      |
| 107 | Peak window time      | Wrong format single digit      | 7:00             | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 108 | Peak window time      | Invalid hour 25:00             | 25:00            | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 109 | Peak window time      | Letter input                   | abc              | Error: use HH:mm format            | Error: invalid time, use HH:mm                | Pass      |
| 110 | Peak window time      | Empty input                    | (empty)          | Error: cannot be blank             | Error: input cannot be blank                  | Pass##    |

## Evaluation

### Achievement of the Requirements

CityRide Lite Part 2 meets all 22 functional requirements set out in the assignment brief. The program supports two roles, rider and admin, each with their own menu and set of permissions. Riders can create and load profiles, add, edit and delete journeys, view running totals, generate daily summaries and export reports. Administrators can log in with a password and manage all configuration values including base fares, passenger discounts, daily caps and the peak window. All changes are saved to file immediately and the system falls back to safe default values if the configuration file is missing on startup. The code follows the NTIC Guide to Good Programming throughout, with one class per file, private fields, meaningful names, single responsibility methods and no while(true) loops.

### Strengths

**1. Robust input validation**

Every user input in the system goes through the `InputHelper` class, which validates all values before they are used. If a user enters a letter where a number is expected, leaves a field blank, or enters a value out of range, the system shows a clear error message and asks again without crashing. This was confirmed by 110 black box tests, all of which passed. This is a strength because it makes the system reliable and safe to use even when the user makes mistakes.

**2. Automatic fare recalculation**

Whenever a journey is edited or deleted, the system automatically recalculates the charged fares and daily cap for all remaining journeys on that day. This means the cap is always correctly applied across the whole day, not just to the journey that was changed. This is a strength because it ensures the data is always accurate without the rider needing to do anything manually.

**3. Safe startup with default configuration**

If the configuration file is missing or cannot be read when the program starts, the system automatically loads default values from `CityRideDataset` and continues running normally. The rider is informed but the program does not crash. This is a strength because it means the system is always usable even in unexpected situations, which is important for reliability.

### Weaknesses

**1. Manual JSON parsing**

The system reads and writes JSON files using a custom manual parser built with `BufferedReader` and `BufferedWriter`. This works correctly for the expected file format but is fragile. If the JSON file is edited manually and a single comma, quote or bracket is misplaced, the parser may silently fail to read a field correctly or skip it entirely without warning. A proper JSON library such as Gson or Jackson would handle any valid JSON format automatically and throw meaningful errors when the file is corrupt. This is a weakness because it increases the risk of data loss and makes the system harder to debug when file issues occur.

**2. Journey IDs are not globally unique**

The assignment brief requires every journey to have a unique ID. Within a single rider's session, the IDs are unique, and each rider has their own separate CSV file so there is no confusion between riders. However, if a rider creates a new profile, the journey ID counter resets to 1. This means two different riders can both have a journey with ID 1, and even within the same rider, loading a new profile and adding journeys will restart from ID 1 again. While this does not cause a functional bug because each rider's journeys are stored separately, it does not fully satisfy the spirit of the unique ID requirement. A globally unique ID system, such as a single shared counter saved across all profiles, would be a more correct solution.

**3. Admin password stored in source code**

The admin password is stored as a constant string directly inside `AdminMenu.java`. Anyone who can read the source code can see the password, and there is no way to change it without editing and recompiling the program. There is also no account lockout after failed attempts, meaning someone could try passwords indefinitely. This is a weakness because it is not a secure approach and would not be acceptable in a real-world system.

**4. Some classes have too many methods**

Classes such as `RiderMenu` and `AdminMenu` have a large number of methods. `RiderMenu` alone handles profile setup, journey management, running totals, summary display, CSV import and export, and saving on exit. While each method does one thing, having so many methods in one class means the class itself is doing too many things at a high level, which is a violation of the Single Responsibility Principle. Ideally some of these responsibilities would be split into smaller dedicated classes.

**5. No data backup or error recovery**

If the program crashes mid-save, for example during a power cut while writing a JSON file, the file could be left partially written and unreadable. The system has no backup mechanism and no way to recover from a corrupted file other than deleting it and starting fresh. This is a weakness because it puts the rider's data at risk in unexpected situations.

**6. No multi-day journey history**

The system is designed around a single active day. While riders can change the active date and journeys from other dates are kept in memory, there is no way to view or manage journeys from previous days in a structured way. The summary and reports only work for the current active date. This is a weakness because in a real transport app a rider would expect to be able to review their full travel history.

### Areas of Improvement

**1. Globally unique journey IDs**

The journey ID system could be improved so that IDs are unique across all riders and all sessions, not just within a single rider's active session. This could be done using a shared counter file similar to how profile IDs are already generated, so each new journey anywhere in the system gets a globally unique number. This would fully satisfy the unique ID requirement in the brief.

**2. Improve Single Responsibility across larger classes**

Classes such as `RiderMenu` and `AdminMenu` could be split into smaller, more focused classes. For example, all summary and report related methods in `RiderMenu` could be moved to a dedicated `ReportMenu` class, and profile setup could be moved to a `ProfileMenu` class. This would make each class easier to read, test and maintain, and would bring the design more closely in line with the Single Responsibility Principle.

**3. Replace manual JSON parsing with a library**

Replacing the custom `JsonFileHandler` parser with a library such as Gson or Jackson would make file reading and writing significantly more reliable. These libraries handle any valid JSON format automatically, including files edited by hand or generated by other programs. This would also reduce the amount of code in `JsonFileHandler` considerably and make it much easier to add new fields to profiles or configuration in the future.

**4. Add journey sorting by time**

Journeys could be automatically sorted by date and time whenever they are displayed or used in cap calculations. This would ensure the daily cap is always applied in the correct chronological order regardless of what order the rider entered the journeys. It could be implemented by sorting the list returned by `getJourneysForDate()` using a comparator on the `dateTime` field.

**5. Add multi-day history and reporting**

The system could be extended to support viewing and reporting across multiple days. A rider could see their full journey history, weekly totals, and how often they reached the daily cap. This would make the application much more useful in a real-world context and is a natural next step after the current single-day design.

### Lessons Learned

Working on this project taught me a number of important lessons about software development that I did not fully appreciate before.

The first and most important lesson was understanding why unique IDs matter in a real system. When I first implemented journey IDs I thought numbering them from 1 per rider was enough. It was only when I looked at the requirement more carefully and thought about what happens across multiple profiles and sessions that I understood why a globally unique ID system is needed. In real systems like TfL every record needs an ID that cannot collide with any other record anywhere in the system, not just within one user's data. This project made that concrete for me in a way that a theoretical explanation would not have.

The second lesson was understanding the Single Responsibility Principle in practice, not just in theory. It is easy to keep adding methods to a class because it seems related. It was only when `RiderMenu` grew to have over twenty methods that I realised how hard it was to find and follow the logic. Breaking things into smaller classes with one clear purpose makes the code much easier to navigate and change later.

The third lesson was about the importance of file handling design early on. Building a custom JSON parser without a library worked but created fragility that I only noticed later. If I started again I would use a proper library from the beginning rather than trying to save time by writing my own.

The fourth lesson was about planning before coding. The IPO table and flowcharts felt like extra work at first, but they actually made the coding stage much clearer because I already knew what each method needed to do before I wrote it. In future projects I would invest even more time in the planning stage.

Finally, this project taught me how much detail goes into something that looks simple on the surface. A basic fare calculator from Part 1 grew into a system with 18 classes, file handling, two roles, cap recalculation, CSV import and export, and 110 tested input cases. Planning and structuring that from the beginning is the only way to keep it manageable.

## Code listing:

#### public class CityRideLite:

```java
import java.util.Scanner;

// This is the entry point of the CityRide Lite application.
// It starts the program, sets up all the objects the system needs,
// and shows the main menu so the user can choose their role.

public class CityRideLite {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        startApp(sc);
        sc.close();
    }

    private static void startApp(Scanner sc) {
        JsonFileHandler jsonFileHandler = new JsonFileHandler();
        ConfigManager configManager = new ConfigManager();
        configManager.loadConfig(jsonFileHandler);


        JourneyManager manager = new JourneyManager();
        SummaryReport summaryReport = new SummaryReport();
        ProfileManager profileManager = new ProfileManager();
        ReportExporter reportExporter = new ReportExporter();
        CsvFileHandler csvFileHandler = new CsvFileHandler();
        RiderMenu riderMenu = new RiderMenu();
        AdminMenu adminMenu = new AdminMenu();

        boolean running = true;
        while (running) {
            int choice = readRoleChoice(sc);

            if (choice == 1) {
                openRiderFlow(sc, manager, summaryReport, profileManager,
                        reportExporter, csvFileHandler, jsonFileHandler, configManager, riderMenu);
            }
            else if (choice == 2) {
                openAdminFlow(sc, adminMenu, configManager, jsonFileHandler);
            }
            else {
                running = false;
            }
        }

        System.out.println("Goodbye!");
    }


    private static int readRoleChoice(Scanner sc) {
        System.out.println("\n=== CityRide Lite ===");
        System.out.println("1. Rider");
        System.out.println("2. Admin");
        System.out.println("0. Exit");

        return InputHelper.readIntInRange(sc, "Enter your choice: ", 0, 2);
    }


    private static void openRiderFlow(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                      ProfileManager profileManager, ReportExporter reportExporter,
                                      CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                                      ConfigManager configManager, RiderMenu riderMenu) {
        riderMenu.showMenu(sc, manager, summaryReport, profileManager,
                reportExporter, csvFileHandler, jsonFileHandler, configManager);
    }


    private static void openAdminFlow(Scanner sc, AdminMenu adminMenu,
                                      ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        adminMenu.showMenu(sc, configManager, jsonFileHandler);
    }
}
```

#### class FareCalculator:

```java
import java.math.BigDecimal;


// Calculates the discounted fare and applies the daily cap for a journey.
// It is kept separate from JourneyManager so that fare logic has one clear home
// and does not get mixed in with the journey storage and retrieval code.

class FareCalculator {

    public BigDecimal discountedFare(SystemConfig config, int fromZone, int toZone,
                                     CityRideDataset.TimeBand band,
                                     CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountRate = config.getDiscount(type);

            if (discountRate != null) {
                BigDecimal discountAmount = baseFare.multiply(discountRate);
                BigDecimal discounted = baseFare.subtract(discountAmount);
                result = MoneyUtil.money(discounted);
            }
        }

        return result;
    }

    public BigDecimal applyCap(SystemConfig config, BigDecimal runningTotal,
                               BigDecimal discountedFare,
                               CityRideDataset.PassengerType type) {

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal cap = config.getDailyCap(type);

        if (cap != null) {
            if (runningTotal.compareTo(cap) >= 0) {
                result = MoneyUtil.money(BigDecimal.ZERO);
            }
            else {
                BigDecimal remaining = cap.subtract(runningTotal);

                if (discountedFare.compareTo(remaining) > 0) {
                    result = MoneyUtil.money(remaining);
                }
                else {
                    result = MoneyUtil.money(discountedFare);
                }
            }
        }

        return result;
    }
}
```

#### class JourneyManager:

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Stores and manages all journeys for the current session.
// It handles adding, editing and deleting journeys, and recalculates
// the daily cap across all journeys whenever one is changed or removed.
// FareCalculator is used here rather than in the menu classes
// to keep all fare logic in one place.

class JourneyManager {

    private List<Journey> journeys;
    private FareCalculator calc;
    private int nextID;

    public JourneyManager() {
        journeys = new ArrayList<>();
        calc = new FareCalculator();
        nextID = 1;
    }

    // Returns all journeys that match the given date
    public List<Journey> getJourneysForDate(LocalDate date) {
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < journeys.size()) {
            if (journeys.get(i).getDate().equals(date)) {
                result.add(journeys.get(i));
            }
            i++;
        }

        return result;
    }

    // Filters journeys by date and passenger type
    // Reuses getJourneysForDate to avoid duplicating the date filter loop
    private List<Journey> getJourneysForDateAndType(LocalDate date, CityRideDataset.PassengerType type) {
        List<Journey> byDate = getJourneysForDate(date);
        List<Journey> result = new ArrayList<>();

        int i = 0;
        while (i < byDate.size()) {
            if (byDate.get(i).getType() == type) {
                result.add(byDate.get(i));
            }
            i++;
        }

        return result;
    }

    // Reapplies the daily cap to all journeys for a given date and passenger type
    // Journeys are processed in list order so chronological sorting should happen before calling this
    public void recalculateChargedFaresForDay(LocalDate date, CityRideDataset.PassengerType type, SystemConfig config) {
        List<Journey> dayJourneys = getJourneysForDateAndType(date, type);
        BigDecimal runningTotal = BigDecimal.ZERO;

        int i = 0;
        while (i < dayJourneys.size()) {
            Journey j = dayJourneys.get(i);
            BigDecimal newCharged = calc.applyCap(config, runningTotal, j.getDiscountedFare(), j.getType());
            j.setChargedFare(newCharged);
            runningTotal = runningTotal.add(newCharged);
            i++;
        }
    }

    // Adds a new journey if the zone/band fare exists in the config
    public boolean addJourney(LocalDateTime dateTime, int fromZone, int toZone,
                              CityRideDataset.TimeBand band, CityRideDataset.PassengerType type,
                              SystemConfig config) {
        boolean added = false;
        BigDecimal baseFare = config.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, fromZone, toZone, band, type);
            BigDecimal runningTotal = getTotalChargedForDay(dateTime.toLocalDate(), type);
            BigDecimal chargedFare = calc.applyCap(config, runningTotal, discountedFare, type);

            int id = nextID;
            nextID++;

            Journey newJourney = new Journey(dateTime, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare);

            journeys.add(newJourney);
            added = true;
        }

        return added;
    }

    // Removes a journey by ID and recalculates the day's caps after removal
    public boolean removeJourneyById(int id, SystemConfig config) {
        boolean removed = false;
        LocalDate removedDate = null;
        CityRideDataset.PassengerType removedType = null;

        int i = 0;
        while (i < journeys.size() && !removed) {
            Journey j = journeys.get(i);

            if (j.getId() == id) {
                removedDate = j.getDate();
                removedType = j.getType();
                journeys.remove(i);
                removed = true;
            }
            else {
                i++;
            }
        }

        if (removed) {
            recalculateChargedFaresForDay(removedDate, removedType, config);
        }

        return removed;
    }

    // Returns the total charged fare for a specific date and passenger type
    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {
        List<Journey> dayJourneys = getJourneysForDateAndType(date, type);
        BigDecimal total = BigDecimal.ZERO;

        int i = 0;
        while (i < dayJourneys.size()) {
            total = total.add(dayJourneys.get(i).getChargedFare());
            i++;
        }

        return total;
    }

    // Recalculates base fare, discount, and discount amount for a single journey
    public void recalculateFaresForJourney(Journey j, SystemConfig config) {
        BigDecimal baseFare = config.getBaseFare(j.getFromZone(), j.getToZone(), j.getBand());

        if (baseFare != null) {
            BigDecimal discountedFare = calc.discountedFare(config, j.getFromZone(), j.getToZone(), j.getBand(), j.getType());
            j.setBaseFare(baseFare);
            j.setDiscountedFare(discountedFare);
            j.setDiscountApplied(MoneyUtil.money(baseFare.subtract(discountedFare)));
        }
    }

    // Replaces all journeys with a new list and resets the ID counter
    public void setJourneys(List<Journey> newJourneys) {
        journeys.clear();
        nextID = 1;

        int i = 0;
        while (i < newJourneys.size()) {
            Journey journey = newJourneys.get(i);
            journeys.add(journey);

            if (journey.getId() >= nextID) {
                nextID = journey.getId() + 1;
            }

            i++;
        }
    }

    // Removes all journeys and resets the ID counter
    public void clearJourneys() {
        journeys.clear();
        nextID = 1;
    }
}
```

### class RiderProfile:

```java
// Represents a single rider's profile with their personal details.
// It stores the profile ID, name, passenger type and default payment option.
// The profile ID is generated once on creation and never changes,
// so the system can always find the right files for that rider.

class RiderProfile {

    enum PaymentOption {
        CARD,
        CASH
    }

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private PaymentOption defaultPaymentOption;
    private String profileId;

    public RiderProfile(String profileId, String name,
                        CityRideDataset.PassengerType passengerType,
                        PaymentOption defaultPaymentOption) {
        this.profileId = profileId;
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPaymentOption = defaultPaymentOption;
    }

    public String getName() {
        return name;
    }

    public CityRideDataset.PassengerType getPassengerType() {
        return passengerType;
    }

    public PaymentOption getDefaultPaymentOption() {
        return defaultPaymentOption;
    }

    public String getProfileId() {
        return profileId;
    }

}
```

#### class ProfileManager:

```java
// Manages the rider's profile for the current session.
// It handles creating a new profile, loading an existing one,
// and saving it back to file when the rider is done.
// The current profile is stored here so other classes can access
// the rider's name, passenger type and payment option.

class ProfileManager {
    private RiderProfile currentProfile;

    // Creates a new profile with a generated unique ID
    public RiderProfile createProfile(String name,
                                      CityRideDataset.PassengerType passengerType,
                                      RiderProfile.PaymentOption paymentOption,
                                      JsonFileHandler jsonFileHandler) {
        String profileId = generateProfileId(jsonFileHandler);
        currentProfile = new RiderProfile(profileId, name, passengerType, paymentOption);
        return currentProfile;
    }

    private String generateProfileId(JsonFileHandler jsonFileHandler) {
        int count = jsonFileHandler.loadProfileCount();
        count++;
        jsonFileHandler.saveProfileCount(count);
        return "R" + count;
    }

    public RiderProfile loadProfile(String profileId, JsonFileHandler jsonFileHandler) {
        RiderProfile profile = jsonFileHandler.loadProfile(profileId + ".json");

        if (profile != null) {
            currentProfile = profile;
        }

        return profile;
    }

    public boolean saveProfile(JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveProfile(currentProfile.getProfileId() + ".json", currentProfile);
    }

    public RiderProfile getCurrentProfile() {

        return currentProfile;
    }

    public boolean hasCurrentProfile() {
        boolean hasProfile = false;

        if (currentProfile != null) {
            hasProfile = true;
        }

        return hasProfile;
    }
}
```

#### class RiderMenu:

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

// Handles everything the rider sees and does in the application.
// It guides the rider through profile setup, journey management,
// running totals, and report exports using a series of menus.
// The active date is stored here because all journey operations
// are scoped to the day the rider is working with.

class RiderMenu {
    private LocalDate activeDate;

    public void showMenu(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                         ProfileManager profileManager, ReportExporter reportExporter,
                         CsvFileHandler csvFileHandler, JsonFileHandler jsonFileHandler,
                         ConfigManager configManager) {

        profileSetupUI(sc, profileManager, jsonFileHandler, manager, csvFileHandler);

        if (profileManager.hasCurrentProfile()) {
            chooseActiveDateUI(sc);

            journeyMenuUI(sc, manager, summaryReport, profileManager,
                    reportExporter, csvFileHandler, configManager);

            saveProfileBeforeExitUI(sc, profileManager, jsonFileHandler);
            saveJourneysBeforeExitUI(sc, manager, csvFileHandler, profileManager);
        }
    }

    private void chooseActiveDateUI(Scanner sc) {
        System.out.println("\n=== Active Day ===");
        activeDate = InputHelper.readDate(sc,
                "Enter active date (DD-MM-YYYY, e.g. 22-04-2026): ");
        System.out.println("Active day set to: " + activeDate);
    }

    private void profileSetupUI(Scanner sc, ProfileManager profileManager,
                                JsonFileHandler jsonFileHandler, JourneyManager manager,
                                CsvFileHandler csvFileHandler) {
        int choice;

        do {
            System.out.println("\n=== CityRide Lite ===");
            System.out.println("1. Create new profile");
            System.out.println("2. Load existing profile");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Enter your choice (0-2, e.g. 1): ", 0, 2);

            if (choice == 1) {
                createProfileUI(sc, profileManager, jsonFileHandler, manager);
            }
            else if (choice == 2) {
                loadProfileUI(sc, profileManager, jsonFileHandler, manager, csvFileHandler);
            }

        } while (choice != 0 && !profileManager.hasCurrentProfile());
    }

    private void createProfileUI(Scanner sc, ProfileManager profileManager,
                                 JsonFileHandler jsonFileHandler, JourneyManager manager) {
        System.out.println("\n=== Create Profile ===");
        String name = InputHelper.readRequiredText(sc, "Enter your name (e.g. Ali Khan): ");
        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        RiderProfile.PaymentOption payment = InputHelper.readPaymentOption(sc,
                "Enter payment option (CARD/CASH): ");

        RiderProfile profile = profileManager.createProfile(name, type, payment, jsonFileHandler);
        profileManager.saveProfile(jsonFileHandler);
        manager.clearJourneys();

        System.out.println("Profile created successfully!");
        System.out.println("Your profile ID is: " + profile.getProfileId());
        System.out.println("Please remember your ID to load your profile next time.");
    }

    private void loadProfileUI(Scanner sc, ProfileManager profileManager,
                               JsonFileHandler jsonFileHandler, JourneyManager manager,
                               CsvFileHandler csvFileHandler) {
        System.out.println("\n=== Load Profile ===");

        List<String> available = jsonFileHandler.listAvailableProfiles();

        if (available.isEmpty()) {
            System.out.println("No profiles found. Please create a new profile.");
        }
        else {
            System.out.println("Available profiles:");
            int i = 0;
            while (i < available.size()) {
                System.out.println("  " + available.get(i));
                i++;
            }

            String profileId = InputHelper.readRequiredText(sc, "Enter your profile ID (e.g. R1): ");
            RiderProfile profile = profileManager.loadProfile(profileId, jsonFileHandler);

            if (profile != null) {
                String journeysFile = profile.getProfileId() + "_journeys.csv";
                List<Journey> loadedJourneys = csvFileHandler.importJourneys(journeysFile);

                manager.setJourneys(loadedJourneys);

                System.out.println("Profile loaded. Welcome back, " + profile.getName() + "!");
                System.out.println("Loaded journeys: " + loadedJourneys.size());
            }
            else {
                System.out.println("ERROR: No profile found with ID " + profileId + ". Please try again.");
            }
        }
    }

    private void journeyMenuUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                               ProfileManager profileManager, ReportExporter reportExporter,
                               CsvFileHandler csvFileHandler, ConfigManager configManager) {
        int choice;

        do {
            String name = profileManager.getCurrentProfile().getName();
            System.out.println("\n=== Journey Menu ===");
            System.out.println("Active day: " + activeDate);
            System.out.println("Welcome, " + name + "!");
            System.out.println("1. Add journey");
            System.out.println("2. Edit journey");
            System.out.println("3. Delete journey");
            System.out.println("4. List journeys");
            System.out.println("5. Calculate/View running totals");
            System.out.println("6. Summary and Reports");
            System.out.println("7. Change active day");
            System.out.println("0. Exit");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-7, e.g. 1): ", 0, 7);

            switch (choice) {
                case 1:
                    addJourneyUI(sc, manager, profileManager, configManager);
                    break;
                case 2:
                    editJourneyUI(sc, manager, configManager);
                    break;
                case 3:
                    deleteJourneyUI(sc, manager, configManager);
                    break;
                case 4:
                    listJourneysUI(manager);
                    break;
                case 5:
                    showRunningTotalsUI(manager, profileManager, configManager);
                    break;
                case 6:
                    summaryAndReportsMenuUI(sc, manager, summaryReport, profileManager,
                            reportExporter, csvFileHandler);
                    break;
                case 7:
                    chooseActiveDateUI(sc);
                    break;
            }

        } while (choice != 0);
    }

    private void addJourneyUI(Scanner sc, JourneyManager manager,
                              ProfileManager profileManager, ConfigManager configManager) {
        System.out.println("\n=== Add Journey ===");
        System.out.println("Active day: " + activeDate);

        LocalTime time = InputHelper.readTime(sc, "Enter time (HH:mm, e.g. 08:30): ");
        LocalDateTime dateTime = LocalDateTime.of(activeDate, time);

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5, e.g. 2): ", 1, 5);
        int toZone = InputHelper.readIntInRange(sc, "Enter to zone (1-5, e.g. 4): ", 1, 5);

        CityRideDataset.TimeBand band =
                configManager.determineBand(dateTime, configManager.getCurrentConfig());

        System.out.println("Time band: " + band + " (set automatically from journey time)");

        CityRideDataset.PassengerType type = profileManager.getCurrentProfile().getPassengerType();
        boolean added = manager.addJourney(dateTime, fromZone, toZone, band, type, configManager.getCurrentConfig());

        if (added) {
            System.out.println("Journey added successfully.");
        }
        else {
            System.out.println("ERROR: Could not add journey. Please check zone values.");
        }
    }

    private void editJourneyUI(Scanner sc, JourneyManager manager, ConfigManager configManager) {
        System.out.println("\n=== Edit Journey ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to edit for the active day.");
        }
        else {
            printJourneys(dayJourneys);

            int id = InputHelper.readIntInRange(sc, "Enter journey ID to edit (e.g. 3): ", 1, 999);
            Journey j = findActiveDayJourneyById(manager, id);

            if (j == null) {
                System.out.println("ERROR: No journey found with ID=" + id + " for the active day.");
            }
            else {
                LocalDate oldDate = j.getDate();
                LocalTime newTime = InputHelper.readTime(sc, "Enter new time (HH:mm, e.g. 09:15): ");
                LocalDateTime newDateTime = LocalDateTime.of(activeDate, newTime);
                CityRideDataset.TimeBand newBand =
                        configManager.determineBand(newDateTime, configManager.getCurrentConfig());

                System.out.println("Time band: " + newBand + " (set automatically from journey time)");

                j.setDateTime(newDateTime);
                j.setBand(newBand);
                j.setFromZone(InputHelper.readIntInRange(sc, "Enter new from zone (1-5, e.g. 2): ", 1, 5));
                j.setToZone(InputHelper.readIntInRange(sc, "Enter new to zone (1-5, e.g. 4): ", 1, 5));

                manager.recalculateFaresForJourney(j, configManager.getCurrentConfig());
                manager.recalculateChargedFaresForDay(oldDate, j.getType(), configManager.getCurrentConfig());
                manager.recalculateChargedFaresForDay(j.getDate(), j.getType(), configManager.getCurrentConfig());

                System.out.println("Journey updated successfully.");
            }
        }
    }

    private void deleteJourneyUI(Scanner sc, JourneyManager manager, ConfigManager configManager) {
        System.out.println("\n=== Delete Journey ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to delete for the active day.");
        }
        else {
            printJourneys(dayJourneys);

            int id = InputHelper.readIntInRange(sc, "Enter journey ID to delete (e.g. 2): ", 1, 999);
            Journey j = findActiveDayJourneyById(manager, id);

            if (j == null) {
                System.out.println("ERROR: No journey found with ID=" + id + " for the active day.");
            }
            else {
                boolean confirm = InputHelper.readYesNo(sc,
                        "Confirm delete journey ID=" + id + " for " + activeDate + "? (Y/N): ");

                if (confirm) {
                    boolean removed = manager.removeJourneyById(id, configManager.getCurrentConfig());

                    if (removed) {
                        System.out.println("Journey deleted and fares recalculated.");
                    }
                    else {
                        System.out.println("ERROR: No journey found with ID=" + id);
                    }
                }
                else {
                    System.out.println("Deletion cancelled.");
                }
            }
        }
    }

    private void listJourneysUI(JourneyManager manager) {
        System.out.println("\n=== Journey List ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys recorded for the active day.");
        }
        else {
            printJourneys(dayJourneys);
        }
    }


    private Journey findActiveDayJourneyById(JourneyManager manager, int id) {
        Journey found = null;
        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        int i = 0;
        while (i < dayJourneys.size() && found == null) {
            if (dayJourneys.get(i).getId() == id) {
                found = dayJourneys.get(i);
            }
            i++;
        }

        return found;
    }

    private void printJourneys(List<Journey> journeys) {
        int i = 0;
        while (i < journeys.size()) {
            System.out.println(journeys.get(i));
            i++;
        }
    }

    private void showRunningTotalsUI(JourneyManager manager, ProfileManager profileManager,
                                     ConfigManager configManager) {
        System.out.println("\n=== Running Totals ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys recorded yet for the active day.");
        }
        else {
            CityRideDataset.PassengerType type = profileManager.getCurrentProfile().getPassengerType();
            BigDecimal total = manager.getTotalChargedForDay(activeDate, type);
            BigDecimal cap = configManager.getCurrentConfig().getDailyCap(type);

            System.out.println("Passenger type: " + type);
            System.out.println("Total charged: GBP " + MoneyUtil.money(total));
            System.out.println("Daily cap: GBP " + MoneyUtil.money(cap));

            if (total.compareTo(cap) >= 0) {
                System.out.println("Cap reached: Yes");
            }
            else {
                System.out.println("Cap reached: No");
            }
        }
    }

    private void summaryAndReportsMenuUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport,
                                         ProfileManager profileManager, ReportExporter reportExporter,
                                         CsvFileHandler csvFileHandler) {
        int choice;

        do {
            System.out.println("\n=== Summary and Reports ===");
            System.out.println("Active day: " + activeDate);
            System.out.println("1. Show daily summary");
            System.out.println("2. Import journeys from CSV");
            System.out.println("3. Export active day journeys to CSV");
            System.out.println("4. Export summary as text");
            System.out.println("5. Export summary as CSV");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-5, e.g. 1): ", 0, 5);

            switch (choice) {
                case 1:
                    showSummaryUI(manager, summaryReport);
                    break;
                case 2:
                    importJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 3:
                    exportJourneysUI(manager, csvFileHandler, profileManager);
                    break;
                case 4:
                    exportSummaryAsTextUI(manager, summaryReport, reportExporter, profileManager);
                    break;
                case 5:
                    exportSummaryAsCsvUI(manager, reportExporter, profileManager);
                    break;
            }

        } while (choice != 0);
    }

    private void showSummaryUI(JourneyManager manager, SummaryReport summaryReport) {
        System.out.println("\n=== Daily Summary ===");
        System.out.println("Active day: " + activeDate);
        summaryReport.printSummary(manager, activeDate);
    }

    private void importJourneysUI(JourneyManager manager, CsvFileHandler csvFileHandler,
                                  ProfileManager profileManager) {
        System.out.println("\n=== Import Journeys ===");

        String filePath = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
        List<Journey> imported = csvFileHandler.importJourneys(filePath);

        if (imported.isEmpty()) {
            System.out.println("No journeys found in " + filePath);
        }
        else {
            manager.setJourneys(imported);
            System.out.println(imported.size() + " journeys imported from " + filePath);
        }
    }


    private void exportSummaryAsTextUI(JourneyManager manager, SummaryReport summaryReport,
                                       ReportExporter reportExporter, ProfileManager profileManager) {
        System.out.println("\n=== Export Summary as Text ===");
        System.out.println("Active day: " + activeDate);

        String riderName = profileManager.getCurrentProfile().getName();
        String filePath  = buildSummaryFileName(profileManager, activeDate, "txt");

        boolean success = reportExporter.exportSummaryAsText(filePath, riderName, activeDate, summaryReport, manager);

        if (success) {
            System.out.println("Text summary exported to " + filePath);
        }
        else {
            System.out.println("ERROR: Could not export summary.");
        }
    }


    private void exportSummaryAsCsvUI(JourneyManager manager,
                                      ReportExporter reportExporter, ProfileManager profileManager) {
        System.out.println("\n=== Export Summary as CSV ===");
        System.out.println("Active day: " + activeDate);

        String riderName = profileManager.getCurrentProfile().getName();
        String filePath  = buildSummaryFileName(profileManager, activeDate, "csv");

        boolean success = reportExporter.exportSummaryAsCsv(filePath, riderName, activeDate, manager);

        if (success) {
            System.out.println("CSV summary exported to " + filePath);
        }
        else {
            System.out.println("ERROR: Could not export CSV summary.");
        }
    }


    private void saveProfileBeforeExitUI(Scanner sc, ProfileManager profileManager,
                                         JsonFileHandler jsonFileHandler) {
        boolean save = InputHelper.readYesNo(sc, "Save your profile? (Y/N): ");

        if (save) {
            boolean success = profileManager.saveProfile(jsonFileHandler);
            if (success) {
                System.out.println("Profile saved.");
            }
            else {
                System.out.println("ERROR: Could not save profile.");
            }
        }
    }
    // Exports only the active day journeys to the rider's CSV file
    private void exportJourneysUI(JourneyManager manager, CsvFileHandler csvFileHandler,
                                  ProfileManager profileManager) {
        System.out.println("\n=== Export Journeys ===");
        System.out.println("Active day: " + activeDate);

        List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);

        if (dayJourneys.isEmpty()) {
            System.out.println("No journeys to export for the active day.");
        }
        else {
            String filePath = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(filePath, dayJourneys);

            if (success) {
                System.out.println("Journeys exported to " + filePath);
            }
            else {
                System.out.println("ERROR: Could not export journeys.");
            }
        }
    }

    private void saveJourneysBeforeExitUI(Scanner sc, JourneyManager manager,
                                          CsvFileHandler csvFileHandler,
                                          ProfileManager profileManager) {
        boolean save = InputHelper.readYesNo(sc, "Save active day journeys? (Y/N): ");

        if (save) {
            List<Journey> dayJourneys = manager.getJourneysForDate(activeDate);
            String fileName = profileManager.getCurrentProfile().getProfileId() + "_journeys.csv";
            boolean success = csvFileHandler.exportJourneys(fileName, dayJourneys);

            if (success) {
                System.out.println("Journeys for " + activeDate + " saved to " + fileName);
            }
            else {
                System.out.println("ERROR: Could not save journeys.");
            }
        }
    }


    private String buildSummaryFileName(ProfileManager profileManager, LocalDate date, String extension) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String profileId = profileManager.getCurrentProfile().getProfileId();
        String safeName  = profileManager.getCurrentProfile().getName().replace(" ", "_");
        return profileId + "_" + safeName + "_" + date.format(dateFormat) + "_summary." + extension;
    }
}
```

#### class ConfigManager:

```java
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Loads, stores and updates the system configuration.
// On start app it tries to read the config file, and falls back to safe
// defaults from CityRideDataset if the file is missing.
// It also determines the time band for a journey automatically
// by comparing the journey time against the active peak window.

class ConfigManager {

    private SystemConfig currentConfig;

    // Loads config from file. If it fails or file is missing, creates safe defaults instead
    public SystemConfig loadConfig(JsonFileHandler jsonFileHandler) {
        SystemConfig config = jsonFileHandler.loadConfig("config.json");

        if (config == null) {
            // File missing or invalid - start with safe default values
            System.out.println("Config file not found. Using default values.");
            config = createDefaultConfig();
        }

        currentConfig = config;
        return currentConfig;
    }

    // Builds a default config using the values already defined in CityRideDataset
    public SystemConfig createDefaultConfig() {
        SystemConfig config = new SystemConfig();

        // Load all base fares from the dataset as defaults
        int from = 1;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = 1;
            while (to <= CityRideDataset.MAX_ZONE) {
                BigDecimal peakFare = CityRideDataset.getBaseFare(from, to, CityRideDataset.TimeBand.PEAK);
                BigDecimal offPeakFare = CityRideDataset.getBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK);
                config.setBaseFare(from, to, CityRideDataset.TimeBand.PEAK, peakFare);
                config.setBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK, offPeakFare);
                to++;
            }
            from++;
        }

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            config.setDiscount(types[i], CityRideDataset.DISCOUNT_RATE.get(types[i]));
            config.setDailyCap(types[i], CityRideDataset.DAILY_CAP.get(types[i]));
            i++;
        }

        return config;
    }

    public CityRideDataset.TimeBand determineBand(LocalDateTime dateTime, SystemConfig config) {
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.OFF_PEAK;

        LocalTime journeyTime = dateTime.toLocalTime();
        LocalTime peakStart = LocalTime.parse(config.getPeakStart());
        LocalTime peakEnd = LocalTime.parse(config.getPeakEnd());

        if (!journeyTime.isBefore(peakStart) && journeyTime.isBefore(peakEnd)) {
            band = CityRideDataset.TimeBand.PEAK;
        }

        return band;
    }

    public boolean saveConfig(SystemConfig config, JsonFileHandler jsonFileHandler) {
        return jsonFileHandler.saveConfig("config.json", config);
    }

    public SystemConfig getCurrentConfig() {
        return currentConfig;
    }

    public void updateBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
        currentConfig.setBaseFare(fromZone, toZone, band, fare);
    }

    public void updateDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
        currentConfig.setDiscount(type, discount);
    }

    public void updateDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
        currentConfig.setDailyCap(type, cap);
    }

    public void updatePeakWindow(String peakStart, String peakEnd) {
        currentConfig.setPeakWindow(peakStart, peakEnd);
    }
}
```

#### class CsvFileHandler:

```java
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Handles reading and writing journey data as CSV files.
// Importing reads an existing file and skips any rows that cannot be parsed,
// so a corrupt line does not stop the rest of the journeys from loading.
// Exporting writes the active day's journeys with a header row
// so the file can be opened in a spreadsheet if needed.


class CsvFileHandler {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public List<Journey> importJourneys(String filePath) {
        List<Journey> journeys = new ArrayList<>();

        try {
            java.io.File file = new java.io.File(filePath);

            if (!file.exists()) {
                return journeys;
            }

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            if (line != null && line.startsWith("id,")) {
                line = reader.readLine();
            }

            while (line != null) {
                Journey journey = parseJourney(line);

                if (journey != null) {
                    journeys.add(journey);
                }

                line = reader.readLine();
            }

            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not import journeys from " + filePath);
        }

        return journeys;
    }

    public boolean exportJourneys(String filePath, List<Journey> journeys) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            writer.write("id,dateTime,fromZone,toZone,band,type,baseFare,discountedFare,chargedFare");
            writer.newLine();

            int i = 0;
            while (i < journeys.size()) {
                Journey j = journeys.get(i);

                writer.write(
                        j.getId() + "," +
                                j.getDateTime().format(DATE_TIME_FORMAT) + "," +
                                j.getFromZone() + "," +
                                j.getToZone() + "," +
                                j.getBand().name() + "," +
                                j.getType().name() + "," +
                                j.getBaseFare().toPlainString() + "," +
                                j.getDiscountedFare().toPlainString() + "," +
                                j.getChargedFare().toPlainString()
                );
                writer.newLine();

                i++;
            }

            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export journeys to " + filePath);
        }

        return success;
    }

    private Journey parseJourney(String line) {
        Journey journey = null;
        String[] parts = line.split(",");

        if (parts.length == 9) {
            try {
                int id = Integer.parseInt(parts[0].trim());
                LocalDateTime dateTime = LocalDateTime.parse(parts[1].trim(), DATE_TIME_FORMAT);
                int fromZone = Integer.parseInt(parts[2].trim());
                int toZone = Integer.parseInt(parts[3].trim());
                CityRideDataset.TimeBand band = CityRideDataset.TimeBand.valueOf(parts[4].trim());
                CityRideDataset.PassengerType type = CityRideDataset.PassengerType.valueOf(parts[5].trim());
                BigDecimal baseFare = new BigDecimal(parts[6].trim());
                BigDecimal discountedFare = new BigDecimal(parts[7].trim());
                BigDecimal chargedFare = new BigDecimal(parts[8].trim());

                journey = new Journey(dateTime, id, fromZone, toZone, band, type,
                        baseFare, discountedFare, chargedFare);
            }
            catch (Exception ex) {
                System.out.println("WARNING: Skipping invalid journey line in CSV.");
            }
        }

        return journey;
    }
}
```

#### class JsonFileHandler:

```java
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Reads and writes JSON files for profiles and system configuration.
// It uses a simple manual parser rather than an external library
// so the program has no dependencies outside of core Java.
// It also tracks how many profiles have been created using a counter file,
// which is how unique profile IDs are generated across sessions.

class JsonFileHandler {

    public boolean saveProfile(String filePath, RiderProfile profile) {
        boolean success = false;

        if (profile == null) {
            System.out.println("ERROR: No profile to save.");
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write("{");
                writer.newLine();
                writer.write("  \"profileId\": \"" + profile.getProfileId() + "\",");
                writer.newLine();
                writer.write("  \"name\": \"" + profile.getName() + "\",");
                writer.newLine();
                writer.write("  \"passengerType\": \"" + profile.getPassengerType().name() + "\",");
                writer.newLine();
                writer.write("  \"defaultPaymentOption\": \"" + profile.getDefaultPaymentOption().name() + "\"");
                writer.newLine();
                writer.write("}");
                writer.close();
                success = true;
            }
            catch (IOException ex) {
                System.out.println("ERROR: Could not save profile to " + filePath);
            }
        }

        return success;
    }

    public RiderProfile loadProfile(String filePath) {
        RiderProfile profile = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            profile = parseProfile(reader);
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not load profile from " + filePath);
        }

        return profile;
    }

    public int loadProfileCount() {
        int count = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("profiles_count.txt"));
            String line = reader.readLine();
            if (line != null) {
                count = Integer.parseInt(line.trim());
            }
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("No profile count file found. Starting from 0.");
        }

        return count;
    }

    public boolean saveProfileCount(int count) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("profiles_count.txt"));
            writer.write(String.valueOf(count));
            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not save profile count.");
        }

        return success;
    }

    // Returns a list of all profile IDs found in the current directory
    public List<String> listAvailableProfiles() {
        List<String> profiles = new ArrayList<>();

        try {
            java.io.File folder = new java.io.File(".");
            java.io.File[] files = folder.listFiles();

            if (files != null) {
                int i = 0;
                while (i < files.length) {
                    String fileName = files[i].getName();
                    if (fileName.startsWith("R") && fileName.endsWith(".json")
                            && !fileName.equals("config.json")) {
                        profiles.add(fileName.replace(".json", ""));
                    }
                    i++;
                }
            }
        }
        catch (Exception ex) {
            System.out.println("ERROR: Could not list profiles.");
        }

        return profiles;
    }

    private RiderProfile parseProfile(BufferedReader reader) {
        String profileId = "";
        String name = "";
        CityRideDataset.PassengerType passengerType = CityRideDataset.PassengerType.ADULT;
        RiderProfile.PaymentOption paymentOption = RiderProfile.PaymentOption.CARD;

        try {
            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (line.startsWith("\"profileId\"")) {
                    profileId = extractValue(line);
                }
                else if (line.startsWith("\"name\"")) {
                    name = extractValue(line);
                }
                else if (line.startsWith("\"passengerType\"")) {
                    try {
                        passengerType = CityRideDataset.PassengerType.valueOf(extractValue(line));
                    }
                    catch (IllegalArgumentException ex) {
                        System.out.println("WARNING: Invalid passenger type in profile file. Using ADULT.");
                    }
                }
                else if (line.startsWith("\"defaultPaymentOption\"")) {
                    try {
                        paymentOption = RiderProfile.PaymentOption.valueOf(extractValue(line));
                    }
                    catch (IllegalArgumentException ex) {
                        System.out.println("WARNING: Invalid payment option in profile file. Using CARD.");
                    }
                }

                line = reader.readLine();
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not read profile data.");
        }

        RiderProfile profile = null;

        if (!name.isEmpty()) {
            profile = new RiderProfile(profileId, name, passengerType, paymentOption);
        }

        return profile;
    }

    public boolean saveConfig(String filePath, SystemConfig config) {
        boolean success = false;

        if (config == null) {
            System.out.println("ERROR: No config to save.");
        } else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write("{");
                writer.newLine();
                writer.write("  \"peakStart\": \"" + config.getPeakStart() + "\",");
                writer.newLine();
                writer.write("  \"peakEnd\": \"" + config.getPeakEnd() + "\",");
                writer.newLine();
                writeDiscounts(writer, config);
                writeCaps(writer, config);
                writeBaseFares(writer, config);
                writer.write("}");
                writer.close();
                success = true;
            }
            catch (IOException ex) {
                System.out.println("ERROR: Could not save config to " + filePath);
            }
        }

        return success;
    }

    public SystemConfig loadConfig(String filePath) {
        SystemConfig config = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            config = parseConfig(reader);
            reader.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not load config from " + filePath);
        }

        return config;
    }

    private void writeDiscounts(BufferedWriter writer, SystemConfig config) {
        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        try {
            while (i < types.length) {
                writer.write("  \"discount_" + types[i].name() + "\": \"" + config.getDiscount(types[i]) + "\",");
                writer.newLine();
                i++;
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not write discounts.");
        }
    }

    private void writeCaps(BufferedWriter writer, SystemConfig config) {
        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        try {
            while (i < types.length) {
                writer.write("  \"cap_" + types[i].name() + "\": \"" + config.getDailyCap(types[i]) + "\",");
                writer.newLine();
                i++;
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not write caps.");
        }
    }

    private void writeBaseFares(BufferedWriter writer, SystemConfig config) {
        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            writeBaseFaresForZone(writer, config, from);
            from++;
        }
    }

    private void writeBaseFaresForZone(BufferedWriter writer, SystemConfig config, int from) {
        int to = CityRideDataset.MIN_ZONE;
        try {
            while (to <= CityRideDataset.MAX_ZONE) {
                BigDecimal peak = config.getBaseFare(from, to, CityRideDataset.TimeBand.PEAK);
                BigDecimal offPeak = config.getBaseFare(from, to, CityRideDataset.TimeBand.OFF_PEAK);
                writer.write("  \"fare_" + from + "_" + to + "_PEAK\": \"" + peak + "\",");
                writer.newLine();
                writer.write("  \"fare_" + from + "_" + to + "_OFF_PEAK\": \"" + offPeak + "\",");
                writer.newLine();
                to++;
            }
        } catch (IOException ex) {
            System.out.println("ERROR: Could not write base fares.");
        }
    }

    private SystemConfig parseConfig(BufferedReader reader) {
        SystemConfig config = new SystemConfig();

        try {
            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (line.startsWith("\"peakStart\"")) {
                    config.setPeakWindow(extractValue(line), config.getPeakEnd());
                }
                else if (line.startsWith("\"peakEnd\"")) {
                    config.setPeakWindow(config.getPeakStart(), extractValue(line));
                }
                else if (line.startsWith("\"discount_")) {
                    parseDiscount(line, config);
                }
                else if (line.startsWith("\"cap_")) {
                    parseCap(line, config);
                }
                else if (line.startsWith("\"fare_")) {
                    parseFare(line, config);
                }

                line = reader.readLine();
            }
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not read config data.");
        }

        return config;
    }

    private void parseDiscount(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String typeName = parts[1].replace("discount_", "");
        config.setDiscount(CityRideDataset.PassengerType.valueOf(typeName), new BigDecimal(extractValue(line)));
    }

    private void parseCap(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String typeName = parts[1].replace("cap_", "");
        config.setDailyCap(CityRideDataset.PassengerType.valueOf(typeName), new BigDecimal(extractValue(line)));
    }

    private void parseFare(String line, SystemConfig config) {
        String[] parts = line.split("\"");
        String[] keyParts = parts[1].replace("fare_", "").split("_", 3);
        int from = Integer.parseInt(keyParts[0]);
        int to = Integer.parseInt(keyParts[1]);
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.valueOf(keyParts[2]);
        config.setBaseFare(from, to, band, new BigDecimal(extractValue(line)));
    }

    private String extractValue(String line) {
        String value = "";

        if (line.endsWith(",")) {
            line = line.substring(0, line.length() - 1);
        }

        String[] parts = line.split("\"");

        if (parts.length >= 4) {
            value = parts[3];
        }

        return value;
    }
}
```

#### class SystemConfig:

```java
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// Holds the active configuration values used by the system at runtime.
// This includes all base fares, passenger discounts, daily caps and the peak window.
// It is populated either from the config file or from default values,
// and updated immediately whenever the admin makes a change.

class SystemConfig {


    private Map<String, BigDecimal> baseFares;
    private Map<CityRideDataset.PassengerType, BigDecimal> discounts;
    private Map<CityRideDataset.PassengerType, BigDecimal> dailyCaps;
    private String peakStart;
    private String peakEnd;

    public SystemConfig() {
        baseFares = new HashMap<>();
        discounts = new HashMap<>();
        dailyCaps = new HashMap<>();
        peakStart = "07:00";
        peakEnd = "09:00";
    }

    public BigDecimal getBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band) {
        return baseFares.get(CityRideDataset.key(fromZone, toZone, band));
    }

    public void setBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
        baseFares.put(CityRideDataset.key(fromZone, toZone, band), fare);
    }

    public BigDecimal getDiscount(CityRideDataset.PassengerType type) {

        return discounts.get(type);
    }

    public void setDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {

        discounts.put(type, discount);
    }

    public BigDecimal getDailyCap(CityRideDataset.PassengerType type) {
        return dailyCaps.get(type);
    }

    public void setDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {

        dailyCaps.put(type, cap);
    }

    public String getPeakStart() {

        return peakStart;
    }

    public String getPeakEnd() {

        return peakEnd;
    }

    public void setPeakWindow(String peakStart, String peakEnd) {
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
    }
}
```

#### class ReportExporter:

```java
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Writes the daily summary to an external file in either text or CSV format.
// It is kept separate from SummaryReport because generating the summary
// and saving it to disk are two different responsibilities.
// File names are built by the calling class to include the rider's profile ID,
// name and active date so each export is uniquely identifiable.

class ReportExporter {

    public boolean exportSummaryAsText(String filePath, String riderName,
                                       LocalDate date, SummaryReport summaryReport,
                                       JourneyManager manager) {
        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Rider: " + riderName);
            writer.newLine();
            writer.write("Date: " + date);
            writer.newLine();
            writer.newLine();
            writer.write(summaryReport.buildSummaryText(manager, date));
            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export summary to " + filePath);
        }

        return success;
    }

    public boolean exportSummaryAsCsv(String filePath, String riderName,
                                      LocalDate date, JourneyManager manager) {
        boolean success = false;
        List<Journey> dayJourneys = manager.getJourneysForDate(date);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            // Report header info
            writer.write("Rider:," + riderName);
            writer.newLine();
            writer.write("Date:," + date);
            writer.newLine();
            writer.newLine();

            // Column headers
            writer.write("id,dateTime,fromZone,toZone,band,type,baseFare,discountApplied,discountedFare,chargedFare");
            writer.newLine();


            BigDecimal totalCharged = BigDecimal.ZERO;
            int i = 0;
            while (i < dayJourneys.size()) {
                Journey j = dayJourneys.get(i);
                writer.write(
                        j.getId() + "," +
                                j.getDateTime().format(fmt) + "," +
                                j.getFromZone() + "," +
                                j.getToZone() + "," +
                                j.getBand().name() + "," +
                                j.getType().name() + "," +
                                j.getBaseFare() + "," +
                                j.getDiscountApplied() + "," +
                                j.getDiscountedFare() + "," +
                                j.getChargedFare()
                );
                writer.newLine();
                totalCharged = totalCharged.add(j.getChargedFare());
                i++;
            }

            writer.newLine();
            writer.write("Total journeys:," + dayJourneys.size());
            writer.newLine();
            writer.write("Total charged (GBP):," + MoneyUtil.money(totalCharged));
            writer.newLine();

            writer.close();
            success = true;
        }
        catch (IOException ex) {
            System.out.println("ERROR: Could not export CSV summary to " + filePath);
        }

        return success;
    }
}
```

#### class AdminMenu:

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

// Provides a password-protected menu for the administrator.
// From here the admin can view and change the system configuration
// including base fares, passenger discounts, daily caps and the peak window.
// All changes are saved to the config file immediately after each update.

class AdminMenu {

    private static final String ADMIN_PASSWORD = "1234";

    public void showMenu(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        boolean loggedIn = loginUI(sc);

        if (!loggedIn) {
            System.out.println("Incorrect password. Access denied.");
            return;
        }

        int choice;
        do {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. View current config");
            System.out.println("2. Manage base fares");
            System.out.println("3. Manage passenger discounts");
            System.out.println("4. Manage daily caps");
            System.out.println("5. Manage peak window");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-5): ", 0, 5);

            switch (choice) {
                case 1:
                    viewConfigUI(configManager);
                    break;
                case 2:
                    manageFaresUI(sc, configManager, jsonFileHandler);
                    break;
                case 3:
                    manageDiscountsUI(sc, configManager, jsonFileHandler);
                    break;
                case 4:
                    manageCapsUI(sc, configManager, jsonFileHandler);
                    break;
                case 5:
                    managePeakWindowUI(sc, configManager, jsonFileHandler);
                    break;
            }
        } while (choice != 0);
    }


    private boolean loginUI(Scanner sc) {
        boolean result = false;

        System.out.println("\n=== Admin Login ===");
        System.out.print("Enter admin password: ");
        String input = sc.nextLine().trim();

        if (input.equals(ADMIN_PASSWORD)) {
            result = true;
        }

        return result;
    }


    private void viewConfigUI(ConfigManager configManager) {
        SystemConfig config = configManager.getCurrentConfig();

        System.out.println("\n=== Current Configuration ===");
        System.out.println("Peak window: " + config.getPeakStart() + " - " + config.getPeakEnd());

        printDiscounts(config);
        printDailyCaps(config);
        printBaseFares(config, CityRideDataset.TimeBand.PEAK, "PEAK");
        printBaseFares(config, CityRideDataset.TimeBand.OFF_PEAK, "OFF-PEAK");
    }


    private void printDiscounts(SystemConfig config) {
        System.out.println("\nPassenger discounts:");

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            BigDecimal percent = config.getDiscount(types[i])
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP);
            System.out.println("  " + types[i] + ": " + percent + "%");
            i++;
        }
    }


    private void printDailyCaps(SystemConfig config) {
        System.out.println("\nDaily caps:");

        CityRideDataset.PassengerType[] types = CityRideDataset.PassengerType.values();
        int i = 0;
        while (i < types.length) {
            System.out.println("  " + types[i] + ": GBP " + config.getDailyCap(types[i]));
            i++;
        }
    }


    private void printBaseFares(SystemConfig config, CityRideDataset.TimeBand band, String label) {
        System.out.println("\nBase fares (" + label + "):");

        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = CityRideDataset.MIN_ZONE;
            while (to <= CityRideDataset.MAX_ZONE) {
                System.out.println("  Zone " + from + " -> " + to + ": GBP " + config.getBaseFare(from, to, band));
                to++;
            }
            from++;
        }
    }


    private void manageFaresUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Base Fares ===");
            System.out.println("1. Add a base fare");
            System.out.println("2. Update a base fare");
            System.out.println("3. Delete a base fare (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateBaseFareUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetBaseFareUI(sc, configManager, jsonFileHandler);
            }
        } while (choice != 0);
    }


    private void updateBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Base Fare ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK (P-Peak/ O-Off Peak): ");
        BigDecimal newFare = InputHelper.readPositiveBigDecimal(sc, "Enter new fare in GBP (e.g. 3.50): ");

        configManager.updateBaseFare(fromZone, toZone, band, newFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetBaseFareUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Base Fare (Restore Default) ===");

        int fromZone = InputHelper.readIntInRange(sc, "Enter from zone (1-5): ", 1, 5);
        int toZone   = InputHelper.readIntInRange(sc, "Enter to zone (1-5): ", 1, 5);
        CityRideDataset.TimeBand band = InputHelper.readTimeBand(sc, "Enter time band (PEAK/OFF-PEAK (P-Peak/ O-Off Peak): ");

        BigDecimal defaultFare = CityRideDataset.getBaseFare(fromZone, toZone, band);
        configManager.updateBaseFare(fromZone, toZone, band, defaultFare);

        System.out.println("Fare deleted and restored to default: GBP " + defaultFare);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageDiscountsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Passenger Discounts ===");
            System.out.println("1. Add a discount");
            System.out.println("2. Update a discount");
            System.out.println("3. Delete a discount (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateDiscountUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetDiscountUI(sc, configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updateDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Passenger Discount ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal discount = InputHelper.readDiscountRate(sc,
                "Enter new discount as decimal (0.00 to 1.00, e.g. 0.25 for 25%): ");

        configManager.updateDiscount(type, discount);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetDiscountUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Discount (Restore Default) ===");
        printDiscounts(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultDiscount = CityRideDataset.DISCOUNT_RATE.get(type);
        configManager.updateDiscount(type, defaultDiscount);

        System.out.println("Discount deleted and restored to default: " +
                defaultDiscount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void manageCapsUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Daily Caps ===");
            System.out.println("1. Add a daily cap");
            System.out.println("2. Update a daily cap");
            System.out.println("3. Delete a daily cap (restore default)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updateDailyCapUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetDailyCapUI(sc, configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updateDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Daily Cap ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");
        BigDecimal newCap = InputHelper.readPositiveBigDecimal(sc,
                "Enter new daily cap in GBP (e.g. 8.00): ");

        configManager.updateDailyCap(type, newCap);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void resetDailyCapUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Daily Cap (Restore Default) ===");
        printDailyCaps(configManager.getCurrentConfig());

        CityRideDataset.PassengerType type = InputHelper.readPassengerType(sc,
                "Enter passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

        BigDecimal defaultCap = CityRideDataset.DAILY_CAP.get(type);
        configManager.updateDailyCap(type, defaultCap);

        System.out.println("Daily cap deleted and restored to default: GBP " + defaultCap);
        saveAndReport(configManager, jsonFileHandler);
    }


    private void managePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        int choice;
        do {
            System.out.println("\n=== Manage Peak Window ===");
            System.out.println("Current: " + configManager.getCurrentConfig().getPeakStart()
                    + " - " + configManager.getCurrentConfig().getPeakEnd());
            System.out.println("1. Add / Set peak window");
            System.out.println("2. Update peak window");
            System.out.println("3. Delete peak window (restore default 07:00 - 09:00)");
            System.out.println("0. Back");

            choice = InputHelper.readIntInRange(sc, "Choose an option (0-3): ", 0, 3);

            if (choice == 1 || choice == 2) {
                updatePeakWindowUI(sc, configManager, jsonFileHandler);
            }
            else if (choice == 3) {
                resetPeakWindowUI(configManager, jsonFileHandler);
            }
        }
        while (choice != 0);
    }


    private void updatePeakWindowUI(Scanner sc, ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Set Peak Window ===");

        String peakStart = InputHelper.readTimeString(sc, "Enter new peak start (HH:mm, e.g. 07:00): ");
        String peakEnd   = InputHelper.readTimeString(sc, "Enter new peak end (HH:mm, e.g. 09:00): ");

        if (peakStart.compareTo(peakEnd) >= 0) {
            System.out.println("ERROR: Start time must be before end time. No changes saved.");
        }
        else {
            configManager.updatePeakWindow(peakStart, peakEnd);
            saveAndReport(configManager, jsonFileHandler);
        }
    }


    private void resetPeakWindowUI(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        System.out.println("\n=== Delete Peak Window (Restore Default) ===");

        configManager.updatePeakWindow("07:00", "09:00");

        System.out.println("Peak window deleted and restored to default: 07:00 - 09:00");
        saveAndReport(configManager, jsonFileHandler);
    }


    private void saveAndReport(ConfigManager configManager, JsonFileHandler jsonFileHandler) {
        boolean saved = configManager.saveConfig(configManager.getCurrentConfig(), jsonFileHandler);

        if (saved) {
            System.out.println("Changes saved successfully.");
        }
        else {
            System.out.println("ERROR: Could not save config.");
        }
    }
}
```

#### class SummaryReport:

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Builds and displays the end-of-day summary for a rider.
// It calculates totals, averages, the most expensive journey, cap savings,
// and counts journeys by time band and zone pair.
// The summary can be printed to the console or returned as a string
// so ReportExporter can write it to a file.

class SummaryReport {

    public void printSummary(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        System.out.println(buildSummaryText(data, date));
        printZonePairCounts(data.getZonePairCounts());
        printZoneCounts(data.getZoneCounts());
    }

    public String buildSummaryText(JourneyManager manager, LocalDate date) {
        SummaryData data = calculateSummaryData(manager, date);
        return buildSummaryText(data, date);
    }

    private String buildSummaryText(SummaryData data, LocalDate date) {
        String result = "";

        result = result + "=== CityRide Lite Daily Summary ===" + "\n";
        result = result + "Date: " + date + "\n";
        result = result + "Total journeys: " + data.getTotalJourneys() + "\n";
        result = result + "Total charged: GBP " + MoneyUtil.money(data.getTotalCharged()) + "\n";
        result = result + "Average cost per journey: GBP " + MoneyUtil.money(data.getAverage()) + "\n";

        if (data.getMostExpensiveId() == -1) {
            result = result + "Most expensive journey: none" + "\n";
        }
        else {
            result = result + "Most expensive journey: ID " + data.getMostExpensiveId()
                    + " (GBP " + MoneyUtil.money(data.getMostExpensiveFare()) + ")" + "\n";
        }

        result = result + "Savings from cap: GBP " + MoneyUtil.money(data.getSavings()) + "\n";

        if (data.getSavings().compareTo(BigDecimal.ZERO) > 0) {
            result = result + "Cap reached: Yes" + "\n";
        }
        else {
            result = result + "Cap reached: No" + "\n";
        }

        result = result + "Peak journeys: " + data.getPeakCount() + "\n";
        result = result + "Off-peak journeys: " + data.getOffPeakCount() + "\n";

        return result;
    }

    private SummaryData calculateSummaryData(JourneyManager manager, LocalDate date) {
        SummaryData data = new SummaryData();
        List<Journey> list = manager.getJourneysForDate(date);

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);

            data.setTotalJourneys(data.getTotalJourneys() + 1);
            data.setTotalCharged(data.getTotalCharged().add(j.getChargedFare()));

            if (data.getMostExpensiveId() == -1 || j.getChargedFare().compareTo(data.getMostExpensiveFare()) > 0) {
                data.setMostExpensiveFare(j.getChargedFare());
                data.setMostExpensiveId(j.getId());
            }

            if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                data.setPeakCount(data.getPeakCount() + 1);
            }
            else {
                data.setOffPeakCount(data.getOffPeakCount() + 1);
            }

            int fromZone = j.getFromZone();
            int toZone = j.getToZone();

            if (isValidZone(fromZone) && isValidZone(toZone)) {
                data.getZonePairCounts()[fromZone][toZone]++;
                data.getZoneCounts()[fromZone]++;

                if (fromZone != toZone) {
                    data.getZoneCounts()[toZone]++;
                }
            }

            i++;
        }

        if (data.getTotalJourneys() > 0) {
            data.setAverage(data.getTotalCharged().divide(new BigDecimal(data.getTotalJourneys()), 2, RoundingMode.HALF_UP)
            );
        }

        data.setSavings(calculateSavings(manager, date));

        return data;
    }

    private BigDecimal calculateSavings(JourneyManager manager, LocalDate date) {
        BigDecimal savings = BigDecimal.ZERO;
        List<Journey> list = manager.getJourneysForDate(date);

        int i = 0;
        while (i < list.size()) {
            Journey j = list.get(i);
            savings = savings.add(j.getDiscountedFare().subtract(j.getChargedFare()));
            i++;
        }

        return MoneyUtil.money(savings);
    }

    private void printZonePairCounts(int[][] zonePairCounts) {
        System.out.println("\nZone pair counts:");
        boolean hasAny = false;

        int from = CityRideDataset.MIN_ZONE;
        while (from <= CityRideDataset.MAX_ZONE) {
            int to = CityRideDataset.MIN_ZONE;
            while (to <= CityRideDataset.MAX_ZONE) {
                if (zonePairCounts[from][to] > 0) {
                    System.out.println(from + "->" + to + ": " + zonePairCounts[from][to]);
                    hasAny = true;
                }
                to++;
            }
            from++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    private void printZoneCounts(int[] zoneCounts) {
        System.out.println("\nZone counts:");
        boolean hasAny = false;

        int zone = CityRideDataset.MIN_ZONE;
        while (zone <= CityRideDataset.MAX_ZONE) {
            if (zoneCounts[zone] > 0) {
                System.out.println("Zone " + zone + ": " + zoneCounts[zone]);
                hasAny = true;
            }
            zone++;
        }

        if (!hasAny) {
            System.out.println("none");
        }
    }

    private boolean isValidZone(int zone) {
        boolean valid = false;

        if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) {
            valid = true;
        }

        return valid;
    }
}
```

#### class SummaryData:

```java
import java.math.BigDecimal;

// A simple data holder used by SummaryReport to carry all summary values
// through the calculation process in one object.
// It exists as a separate class so that calculateSummaryData() can return
// multiple values cleanly without needing ten separate method calls.


class SummaryData {

    private int totalJourneys;
    private int mostExpensiveId;
    private int peakCount;
    private int offPeakCount;
    private int[][] zonePairCounts;
    private int[] zoneCounts;
    private BigDecimal average;
    private BigDecimal savings;
    private BigDecimal totalCharged;
    private BigDecimal mostExpensiveFare;

    SummaryData() {
        totalJourneys = 0;
        mostExpensiveId = -1;
        peakCount = 0;
        offPeakCount = 0;
        zonePairCounts = new int[CityRideDataset.MAX_ZONE + 1][CityRideDataset.MAX_ZONE + 1];
        zoneCounts = new int[CityRideDataset.MAX_ZONE + 1];
        average = BigDecimal.ZERO;
        savings = BigDecimal.ZERO;
        totalCharged = BigDecimal.ZERO;
        mostExpensiveFare = BigDecimal.ZERO;
    }

    public int getTotalJourneys() { return totalJourneys; }
    public int getMostExpensiveId() { return mostExpensiveId; }
    public int getPeakCount() { return peakCount; }
    public int getOffPeakCount() { return offPeakCount; }
    public int[][] getZonePairCounts() { return zonePairCounts; }
    public int[] getZoneCounts() { return zoneCounts; }
    public BigDecimal getAverage() { return average; }
    public BigDecimal getSavings() { return savings; }
    public BigDecimal getTotalCharged() { return totalCharged; }
    public BigDecimal getMostExpensiveFare() { return mostExpensiveFare; }

    public void setTotalJourneys(int totalJourneys) { this.totalJourneys = totalJourneys; }
    public void setMostExpensiveId(int mostExpensiveId) { this.mostExpensiveId = mostExpensiveId; }
    public void setPeakCount(int peakCount) { this.peakCount = peakCount; }
    public void setOffPeakCount(int offPeakCount) { this.offPeakCount = offPeakCount; }
    public void setAverage(BigDecimal average) { this.average = average; }
    public void setSavings(BigDecimal savings) { this.savings = savings; }
    public void setTotalCharged(BigDecimal totalCharged) { this.totalCharged = totalCharged; }
    public void setMostExpensiveFare(BigDecimal mostExpensiveFare) { this.mostExpensiveFare = mostExpensiveFare; }
}
```

#### class Journey:

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Represents a single journey taken by a rider on a specific day.
// It stores all the fare details calculated at the time of the journey,
// including the base fare, the discount applied and the charged fare after the cap.
// Zones crossed is calculated automatically from the from and to zones
// so it always stays accurate when either zone is updated.

class Journey {
    private LocalDateTime dateTime;
    private int fromZone;
    private int toZone;
    private int zonesCrossed;

    private CityRideDataset.TimeBand band;
    private CityRideDataset.PassengerType type;

    private BigDecimal baseFare;
    private BigDecimal discountedFare;
    private BigDecimal discountApplied;
    private BigDecimal chargedFare;

    private int id;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Journey(LocalDateTime dateTime, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.dateTime = dateTime;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - fromZone) + 1;
        this.band = band;
        this.type = type;
        this.baseFare = baseFare;
        this.discountedFare = discountedFare;
        this.discountApplied = MoneyUtil.money(baseFare.subtract(discountedFare));
        this.chargedFare = chargedFare;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public CityRideDataset.PassengerType getType() {
        return type;
    }

    public CityRideDataset.TimeBand getBand() {
        return band;
    }

    public int getFromZone() {
        return fromZone;
    }

    public int getToZone() {
        return toZone;
    }

    public int getZonesCrossed() {
        return zonesCrossed;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDiscountedFare() {
        return discountedFare;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public BigDecimal getChargedFare() {
        return chargedFare;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setFromZone(int fromZone) {
        this.fromZone = fromZone;
        this.zonesCrossed = Math.abs(this.toZone - fromZone) + 1;
    }

    public void setToZone(int toZone) {
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - this.fromZone) + 1;
    }

    public void setBand(CityRideDataset.TimeBand band) {
        this.band = band;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public void setDiscountedFare(BigDecimal discountedFare) {
        this.discountedFare = discountedFare;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public void setChargedFare(BigDecimal chargedFare) {
        this.chargedFare = chargedFare;
    }

    public String toString() {
        String capApplied = "No";
        if (chargedFare.compareTo(discountedFare) < 0) {
            capApplied = "Yes";
        }

        return "ID: " + id
                + " | " + dateTime.format(DATE_TIME_FORMAT)
                + " | " + type
                + " | " + band
                + " | " + fromZone + "->" + toZone
                + " | Zones crossed: " + zonesCrossed
                + " | Base: GBP " + baseFare
                + " | Discount: GBP " + discountApplied
                + " | Discounted: GBP " + discountedFare
                + " | Charged: GBP " + chargedFare
                + " | Cap applied: " + capApplied;
    }
}
```

#### class Money:

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

// A small utility class that rounds any BigDecimal value to two decimal places.
// It is used throughout the system wherever a fare or total is displayed or stored,
// so rounding is always consistent and never done inline in other methods.

class MoneyUtil {
    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
```

#### class InputHelper:

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// A utility class that handles all user input from the console.
// Every method keeps asking until the user enters a valid value,
// so no other class needs to worry about validation or error handling.
// All methods are static because InputHelper holds no state of its own.


class InputHelper {

    public static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        boolean valid = false;
        int value = min;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    value = Integer.parseInt(input);

                    if (value < min || value > max) {
                        System.out.println("Please enter a number from " + min + " to " + max + ".");
                    }
                    else {
                        valid = true;
                    }

                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a whole number.");
                }
            }
        }

        return value;
    }
    public static LocalTime readTime(Scanner scanner, String prompt) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        boolean valid = false;
        LocalTime time = LocalTime.now();

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    time = LocalTime.parse(input, timeFormat);
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid time. Use HH:mm (e.g. 08:30).");
                }
            }
        }

        return time;
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        boolean valid = false;
        boolean answer = false;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("Y") || input.equals("YES")) {
                answer = true;
                valid = true;
            }
            else if (input.equals("N") || input.equals("NO")) {
                answer = false;
                valid = true;
            }
            else {
                System.out.println("Please enter Y or N.");
            }
        }

        return answer;
    }

    public static String readRequiredText(Scanner scanner, String prompt) {
        boolean valid = false;
        String text = "";

        while (!valid) {
            System.out.print(prompt);
            text = scanner.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                valid = true;
            }
        }

        return text;
    }

    public static CityRideDataset.TimeBand readTimeBand(Scanner scanner, String prompt) {
        boolean valid = false;
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.PEAK;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("P") || input.equals("PEAK")) {
                band = CityRideDataset.TimeBand.PEAK;
                valid = true;
            }
            else if (input.equals("O") || input.equals("OFF-PEAK")
                    || input.equals("OFF_PEAK") || input.equals("OFFPEAK")) {
                band = CityRideDataset.TimeBand.OFF_PEAK;
                valid = true;
            }
            else {
                System.out.println("Invalid time band. Enter PEAK or OFF-PEAK.");
            }
        }

        return band;
    }

    public static CityRideDataset.PassengerType readPassengerType(Scanner scanner, String prompt) {
        boolean valid = false;
        CityRideDataset.PassengerType type = CityRideDataset.PassengerType.ADULT;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("ADULT")) {
                type = CityRideDataset.PassengerType.ADULT;
                valid = true;
            }
            else if (input.equals("STUDENT")) {
                type = CityRideDataset.PassengerType.STUDENT;
                valid = true;
            }
            else if (input.equals("CHILD")) {
                type = CityRideDataset.PassengerType.CHILD;
                valid = true;
            }
            else if (input.equals("SENIOR") || input.equals("SENIOR CITIZEN")
                    || input.equals("SENIOR_CITIZEN") || input.equals("SENIORCITIZEN")) {
                type = CityRideDataset.PassengerType.SENIOR_CITIZEN;
                valid = true;
            }
            else {
                System.out.println("Invalid passenger type. Allowed: Adult, Student, Child, Senior Citizen.");
            }
        }

        return type;
    }

    public static RiderProfile.PaymentOption readPaymentOption(Scanner scanner, String prompt) {
        boolean valid = false;
        RiderProfile.PaymentOption paymentOption = RiderProfile.PaymentOption.CARD;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("CARD")) {
                paymentOption = RiderProfile.PaymentOption.CARD;
                valid = true;
            }
            else if (input.equals("CASH")) {
                paymentOption = RiderProfile.PaymentOption.CASH;
                valid = true;
            }
            else {
                System.out.println("Invalid payment option. Enter CARD or CASH.");
            }
        }

        return paymentOption;
    }

    public static BigDecimal readPositiveBigDecimal(Scanner scanner, String prompt) {
        boolean valid = false;
        BigDecimal value = BigDecimal.ZERO;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    value = new BigDecimal(input);

                    if (value.compareTo(BigDecimal.ZERO) <= 0) {
                        System.out.println("Value must be greater than zero.");
                    }
                    else {
                        valid = true;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number (e.g. 3.50).");
                }
            }
        }

        return value;
    }

    public static BigDecimal readDiscountRate(Scanner scanner, String prompt) {
        boolean valid = false;
        BigDecimal value = BigDecimal.ZERO;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            } else {
                try {
                    value = new BigDecimal(input);

                    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
                        System.out.println("Discount must be between 0.00 and 1.00.");
                    } else {
                        valid = true;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Please enter a valid decimal (e.g. 0.25).");
                }
            }
        }

        return value;
    }

    // Reads a time string in HH:mm format - used for peak window start and end
    public static String readTimeString(Scanner scanner, String prompt) {
        boolean valid = false;
        String time = "";

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            } else {
                try {
                    java.time.LocalTime.parse(input);
                    time = input;
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid time. Use HH:mm format (e.g. 07:00).");
                }
            }
        }

        return time;
    }
    public static LocalDate readDate(Scanner scanner, String prompt) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        boolean valid = false;
        LocalDate date = LocalDate.now();

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
            }
            else {
                try {
                    date = LocalDate.parse(input, dateFormat);
                    valid = true;
                }
                catch (DateTimeParseException e) {
                    System.out.println("Invalid date. Use DD-MM-YYYY (e.g. 22-04-2026).");
                }
            }
        }

        return date;
    }
}
```

#### final class CityRideDataset:

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

// Holds the default fare data and passenger type values for the system.
// It is marked as final because it is a fixed reference dataset
// and should never be extended or modified.
// ConfigManager reads from here when creating default config values,
// and AdminMenu reads from here when restoring a deleted config entry.

final class CityRideDataset {

    private CityRideDataset() {
    }

    public static final int MIN_ZONE = 1;
    public static final int MAX_ZONE = 5;

    public enum TimeBand {
        PEAK,
        OFF_PEAK
    }

    public enum PassengerType {
        ADULT,
        STUDENT,
        CHILD,
        SENIOR_CITIZEN
    }

    public static final Map<PassengerType, BigDecimal> DISCOUNT_RATE = Map.of(
            PassengerType.ADULT, new BigDecimal("0.00"),
            PassengerType.STUDENT, new BigDecimal("0.25"),
            PassengerType.CHILD, new BigDecimal("0.50"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("0.30")
    );

    public static final Map<PassengerType, BigDecimal> DAILY_CAP = Map.of(
            PassengerType.ADULT, new BigDecimal("8.00"),
            PassengerType.STUDENT, new BigDecimal("6.00"),
            PassengerType.CHILD, new BigDecimal("4.00"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("7.00")
    );

    public static final Map<String, BigDecimal> BASE_FARE = buildBaseFare();

    public static BigDecimal getBaseFare(int fromZone, int toZone, TimeBand timeBand) {
        return BASE_FARE.get(key(fromZone, toZone, timeBand));
    }

    public static String key(int fromZone, int toZone, TimeBand timeBand) {
        return fromZone + "-" + toZone + "-" + timeBand.name();
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<String, BigDecimal> buildBaseFare() {
        Map<String, BigDecimal> m = new HashMap<>();

        // Peak fares
        put(m, 1, 1, TimeBand.PEAK, "2.50");
        put(m, 1, 2, TimeBand.PEAK, "3.20");
        put(m, 1, 3, TimeBand.PEAK, "3.80");
        put(m, 1, 4, TimeBand.PEAK, "4.40");
        put(m, 1, 5, TimeBand.PEAK, "5.00");

        put(m, 2, 1, TimeBand.PEAK, "3.20");
        put(m, 2, 2, TimeBand.PEAK, "2.30");
        put(m, 2, 3, TimeBand.PEAK, "3.10");
        put(m, 2, 4, TimeBand.PEAK, "3.80");
        put(m, 2, 5, TimeBand.PEAK, "4.50");

        put(m, 3, 1, TimeBand.PEAK, "3.80");
        put(m, 3, 2, TimeBand.PEAK, "3.10");
        put(m, 3, 3, TimeBand.PEAK, "2.10");
        put(m, 3, 4, TimeBand.PEAK, "3.00");
        put(m, 3, 5, TimeBand.PEAK, "3.70");

        put(m, 4, 1, TimeBand.PEAK, "4.40");
        put(m, 4, 2, TimeBand.PEAK, "3.80");
        put(m, 4, 3, TimeBand.PEAK, "3.00");
        put(m, 4, 4, TimeBand.PEAK, "2.00");
        put(m, 4, 5, TimeBand.PEAK, "2.90");

        put(m, 5, 1, TimeBand.PEAK, "5.00");
        put(m, 5, 2, TimeBand.PEAK, "4.50");
        put(m, 5, 3, TimeBand.PEAK, "3.70");
        put(m, 5, 4, TimeBand.PEAK, "2.90");
        put(m, 5, 5, TimeBand.PEAK, "1.90");

        // Off-peak fares
        put(m, 1, 1, TimeBand.OFF_PEAK, "2.00");
        put(m, 1, 2, TimeBand.OFF_PEAK, "2.70");
        put(m, 1, 3, TimeBand.OFF_PEAK, "3.20");
        put(m, 1, 4, TimeBand.OFF_PEAK, "3.70");
        put(m, 1, 5, TimeBand.OFF_PEAK, "4.20");

        put(m, 2, 1, TimeBand.OFF_PEAK, "2.70");
        put(m, 2, 2, TimeBand.OFF_PEAK, "1.90");
        put(m, 2, 3, TimeBand.OFF_PEAK, "2.60");
        put(m, 2, 4, TimeBand.OFF_PEAK, "3.20");
        put(m, 2, 5, TimeBand.OFF_PEAK, "3.80");

        put(m, 3, 1, TimeBand.OFF_PEAK, "3.20");
        put(m, 3, 2, TimeBand.OFF_PEAK, "2.60");
        put(m, 3, 3, TimeBand.OFF_PEAK, "1.70");
        put(m, 3, 4, TimeBand.OFF_PEAK, "2.50");
        put(m, 3, 5, TimeBand.OFF_PEAK, "3.10");

        put(m, 4, 1, TimeBand.OFF_PEAK, "3.70");
        put(m, 4, 2, TimeBand.OFF_PEAK, "3.20");
        put(m, 4, 3, TimeBand.OFF_PEAK, "2.50");
        put(m, 4, 4, TimeBand.OFF_PEAK, "1.60");
        put(m, 4, 5, TimeBand.OFF_PEAK, "2.40");

        put(m, 5, 1, TimeBand.OFF_PEAK, "4.20");
        put(m, 5, 2, TimeBand.OFF_PEAK, "3.80");
        put(m, 5, 3, TimeBand.OFF_PEAK, "3.10");
        put(m, 5, 4, TimeBand.OFF_PEAK, "2.40");
        put(m, 5, 5, TimeBand.OFF_PEAK, "1.50");

        return Map.copyOf(m);
    }

    private static void put(Map<String, BigDecimal> m, int from, int to, TimeBand band, String amount) {
        m.put(key(from, to, band), money(amount));
    }
}
```

## References

Attacomsian. (2019). *How to read and write JSON files using core Java.* Retrieved from: https://attacomsian.com/blog/java-read-write-json-files [Accessed 7 April 2026].

Barnes, D. and Kolling, M. (2016). *Objects First with Java: A Practical Introduction Using BlueJ.* London: Pearson.

Nkamphoa. (2026). *LocalDateTime class in Java.* Retrieved from: https://nkamphoa.com/localdatetime-class-in-java/ [Accessed 4 April 2026].

Oracle. (2021). *BigDecimal — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html [Accessed 14 April 2026].

Oracle. (2021). *BufferedReader — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedReader.html [Accessed 9 April 2026].

Oracle. (2021). *BufferedWriter — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedWriter.html [Accessed 9 April 2026].

Oracle. (2021). *DateTimeFormatter — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html [Accessed 11 April 2026].

Oracle. (2021). *IOException — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/IOException.html [Accessed 12 April 2026].

Oracle. (2021). *String — Java SE 21 and JDK 21.* Retrieved from: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html [Accessed 16 April 2026].

Oracle. (n.d.). *The catch blocks — The Java Tutorials: Essential Java Classes — Exceptions.* Retrieved from: https://docs.oracle.com/javase/tutorial/essential/exceptions/catch.html [Accessed 13 April 2026].

Splitwise Inc. (2025). *Splitwise — split expenses with friends* [Web and mobile application]. Retrieved from: https://www.splitwise.com [Accessed 21 March 2026].

Stack Abuse. (2019). *Reading and writing CSVs in Java.* Retrieved from: https://stackabuse.com/reading-and-writing-csvs-in-java/ [Accessed 5 April 2026].

Transport for London. (2025). *Contactless and Oyster account.* Retrieved from: https://tfl.gov.uk/fares/contactless-and-oyster-account [Accessed 21 March 2026].

## Appendices:

#### Appendix A - Milestone 1:

---

### Purpose of the Program

---

CityRide Lite Part 2 is a Java console program designed to help riders manage their daily travel and allow admins to manage the fare system. This program is an extension of Part 1 and continues to build on the earlier version to create a more realistic system that could be used in a real-world situation. The program includes two roles, Rider and Admin, and each role has its own permissions and responsibilities. The rider can create a profile, add and manage journeys, calculate fares, and save or export data. The admin can access a protected menu to manage base fares, discounts, daily caps, and peak time settings. Compared with Part 1, this version is more realistic because it supports user roles, file storage, and detailed reporting.

### Core program functionality:

- **Role selection**
  
  - When the program starts, the user chooses whether to enter the system as a Rider or an Admin.
  
  - The program then opens the correct menu and functions for that role.

- **Load configuration**
  
  - On launch, the program loads the system configuration so that the correct fare settings are available.
  
  - This allows the system to use the current fare rules during the session.

- **Profile management**
  
  - The Rider can create, load, and save a personal profile.
  
  - The profile stores details such as the rider’s name, passenger type, and default payment option.

- **Journey management**
  
  - The Rider can add, edit, and delete journeys for the active day.
  
  - Each journey records the main travel details needed for fare calculation and reporting.

- **Import and export journeys**
  
  - The Rider can import journeys from a file instead of entering them one by one.
  
  - The Rider can also export current journey data for later use or review.

- **Fare calculation**
  
  - The program calculates the fare for each journey.
  
  - It works out the correct cost by using journey details, fare rules, discounts, and caps.

- **View journey costs and totals**
  
  - The Rider can view the cost of each journey and the running total for the day.
  
  - The system also shows whether a cap or pass has affected the total.

- **Generate summaries and reports**
  
  - The program creates an end-of-day summary of the rider’s travel.
  
  - It can also produce detailed reports for review and saving.

- **Admin management**
  
  - The Admin can access a separate administration menu.
  
  - From this area, the Admin can view and manage the active fare configuration, including fares, discounts, caps, and peak windows.

### System constraints

- **Menu-driven system**
  
  - The system must be menu-driven, with clear options for profiles, journeys, calculate, reports, save/exit, and admin.
  
  - All prompts must include the expected format and example values.

- **Two fixed user roles**
  
  - The system must support only two roles: Rider and Admin.
  
  - Each role must only access the functions that belong to it.

- **Safe startup rule**
  
  - If the configuration file is missing, the system must still start by using safe default values.

- **File format rules**
  
  - The system must read and write JSON for configuration and rider profiles.
  
  - The system must read and write CSV for journeys and reports.

- **Journey data requirements**
  
  - Each journey must include a unique ID, date and time, from zone, to zone, time band, passenger type, zones crossed, base fare, discount applied, and charged fare.
  
  - Journey data must remain complete and accurate when journeys are added, edited, imported, or exported.

- **Fare rule constraints**
  
  - The system must apply base fares, passenger discounts, and daily caps using the active fare rules.
  
  - The final daily total must not go above the cap for the passenger type.

- **Validation rules**
  
  - Values entered must be validated before they are saved.
  
  - If the input is invalid, the system must show clear error messages and must not save incorrect data.
  
  - Invalid journey IDs must also be rejected with an error message.

- **Recalculation after changes**
  
  - If a journey is deleted or undone, the system must recalculate totals.
  
  - This is needed to keep fare totals and cap results correct.

- **Admin security**
  
  - The admin menu must be password protected.
  
  - Only the Admin should be able to change fare configuration settings.

- **Use of the supplied dataset**
  
  - The program must follow the rules and constraints from CityRideDataset.java.
  
  - The dataset provides the fare-related rules the system must use.

- **Save on exit**
  
  - When the user exits the program, the system must offer to save the rider’s current day state.
  
  - This includes the rider profile and journeys.

---

### Input Process Output Table

---

| Feature/task                | Inputs                                                                          | Processing (what the system does)                                                                                                                                                               | Outputs                                           |
| --------------------------- | ------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| Start program               | Program launch                                                                  | Start the system, load the configuration data, and prepare the menus and program state. If the config file is missing, start with safe default values.                                          | Main menu displayed and configuration ready       |
| Select role                 | User role choice (Rider/Admin)                                                  | Validate the selected role and open the correct menu for Rider or Admin.                                                                                                                        | Rider menu or Admin login prompt displayed        |
| Create rider profile        | Rider name, passenger type, default payment option                              | Validate the entered profile details, create a new rider profile, and prepare it for use in the session.                                                                                        | New rider profile created or error message        |
| Load rider profile          | JSON profile file                                                               | Read the rider profile from JSON, validate the saved values, and load the profile into the system.                                                                                              | Rider profile loaded or error message             |
| Save rider profile          | Current rider profile data                                                      | Convert the profile data into JSON format and save it to file.                                                                                                                                  | Profile saved confirmation or error message       |
| Display rider menu          | Rider menu choice                                                               | Show the available rider options and validate the selected menu choice before routing to the correct function.                                                                                  | Selected rider function opened or error message   |
| Add journey                 | Date/time, from zone, to zone, time band, passenger type                        | Validate the journey details, calculate zones crossed, get the base fare, apply discount and cap rules, assign a unique journey ID, and store the journey under the rider profile.              | Journey added confirmation or validation error    |
| Edit journey                | Journey ID, updated journey details                                             | Check that the journey ID exists, validate the new values, update the journey, and recalculate totals so the fare data stays correct.                                                           | Journey updated confirmation or error message     |
| Delete journey              | Journey ID                                                                      | Check that the journey ID exists,  remove the journey, and recalculate totals and cap results.                                                                                                  | Journey deleted confirmation or error message     |
| Confirmation                | Y/N from the Rider                                                              | Confirm the user choice before delteing journey, editing and exiting the game.                                                                                                                  | After the user confirmation implement the task.   |
| Import journeys from CSV    | CSV file with journey data                                                      | Read each row from the file, validate the journey values, calculate any missing fare values where needed, and add valid journeys to the rider profile. Reject invalid rows with clear messages. | Imported journeys added and import result message |
| Export journeys to CSV      | Stored journeys                                                                 | Collect the current journey data, format it into CSV structure, and write it to a file.                                                                                                         | CSV file created or error message                 |
| Calculate fare              | From zone, to zone, time band, passenger type, running total, active fare rules | Calculate zones crossed, find the base fare, apply the passenger discount, and apply the daily cap so the total does not go above the allowed limit.                                            | Charged fare, updated total, and cap status       |
| View journey costs          | Stored journeys                                                                 | Retrieve the rider’s journeys and display the fare details for each journey.                                                                                                                    | Per-journey costs displayed                       |
| View running totals         | Stored journeys, calculated totals                                              | Calculate or retrieve the current total cost for the day and show whether a cap or pass has been applied.                                                                                       | Running total and cap/pass status displayed       |
| Generate end-of-day summary | Stored journeys for the active day                                              | Calculate total journeys, total cost, average cost per journey, most expensive journey, savings from caps, and category counts such as peak/off-peak journeys.                                  | End-of-day summary displayed                      |
| Export summary reports      | Summary data, rider name, date                                                  | Format the summary into a CSV report and a human-readable text report, then save both files using suitable names.                                                                               | Report files saved or error message               |
| Admin login                 | Admin password                                                                  | Check the entered password and allow access only if it is correct.                                                                                                                              | Admin menu displayed or access denied message     |
| Display admin menu          | Admin menu choice                                                               | Show the available admin options and validate the selected choice before opening the chosen admin function.                                                                                     | Selected admin function opened or error message   |
| View active configuration   | Current config data                                                             | Read the active fare settings, discounts, caps, and peak windows from memory or file and display them clearly.                                                                                  | Current configuration displayed                   |
| Update base fares           | Zones, peak/off-peak setting, new fare value                                    | Validate the new fare data and update the base fare configuration before saving the change.                                                                                                     | Base fare updated or validation error             |
| Update discounts            | Passenger type, discount value                                                  | Validate the discount value and update the discount configuration before saving.                                                                                                                | Discount updated or validation error              |
| Update daily caps           | Passenger type, cap value                                                       | Validate the cap value and update the daily cap configuration before saving.                                                                                                                    | Daily cap updated or validation error             |
| Update peak windows         | Peak start time, peak end time                                                  | Validate the peak window values and update the configuration before saving.                                                                                                                     | Peak window updated or validation error           |
| Save current day state      | Rider profile, journeys, user confirmation                                      | Ask whether the user wants to save the current day state. If yes, save the profile and journeys to file.                                                                                        | Save confirmation or exit without saving          |
| Exit program                | Exit menu choice                                                                | Close the current menu loop and end the program safely.                                                                                                                                         | Goodbye message                                   |

---

### Algorithm Design

#### Flowcharts:![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchart_1.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_2.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_3.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_4.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_5.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_6.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_7.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_8.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_9.jpg)

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchar_10.jpg)

#### Class diagram:

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Class%20diagram.png)

### Research

---

#### Source 1

**Name of program:** TfL Contactless and Oyster Account

**Reference:** Transport for London. (2025). *Contactless and Oyster account*. Available at: [Transport for London](https://tfl.gov.uk/fares/contactless-and-oyster-account) [Accessed 21 March 2026]

**What it does well:**

- Once logged in, users can check their balance, view journey history, manage auto top-up settings, and update their personal details demonstrating how a real transport system links a rider's name, payment preferences, and journey records under a single profile, directly mirroring CityRide Lite Part 2's rider profile structure (name, passenger type, default payment option) saved and loaded between sessions.
- Journey history displays each trip with its date, fare charged, zones, and cap status and users can select a custom date range to filter their records showing how per-journey cost data and daily summaries can be presented clearly to the rider, which maps directly onto CityRide Lite's requirements to view per-journey costs, running totals, and the end-of-day summary report.
- The latest TfL Go update allows customers to easily access their contactless or Oyster account and view their journey history directly in the app, as well as check and apply for refunds for incomplete journeys demonstrating how editing and correcting journey records within a profile-based system should work, which informs CityRide Lite's edit and delete journey features with totals recalculation.

**What it does poorly:**

- Oyster journey history is only stored for up to eight weeks after which it is permanently deleted meaning riders cannot access older records for expense tracking or auditing purposes. CityRide Lite avoids this limitation by saving the rider's full journey data locally to a JSON file, giving the rider permanent offline access to their own records

**Key design ideas you could reuse:**

- The profile-linked journey history model where a logged-in rider sees only their own journeys, costs, and cap status is the exact model for CityRide Lite's rider session: load a profile, add journeys under it, and view a personalised summary.
- The date-grouped journey display (journeys grouped by day with totals shown beneath each group) is a strong model for CityRide Lite's end-of-day summary layout.
- The save-on-exit pattern where the system saves your card state and session so you can continue next time directly informs CityRide Lite's requirement to offer to save the rider's profile and journeys when the program exits.

**Screenshot:**
![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Source%201.png)

#### Source 2

**Name of program:** Splitwise

**Reference:** Splitwise Inc. (2025). *Splitwise — split expenses with friends* [Web and mobile application]. Available at: [https://www.splitwise.com](https://www.splitwise.com) [Accessed 21 March 2026]

**What it does well:**

- Splitwise keeps a running total over time per user, records all individual transactions with amounts and categories, and lets users add, edit, or delete any expense with a full edit history showing every change made directly mirroring CityRide Lite Part 2's requirements to add, edit, and delete journeys with totals recalculated after every change. [Top Tip London](https://www.toptiplondon.com/transport/getting-around-london/london-journey-planner)
- Users can export all their expense data to a CSV file from the web interface with a single click demonstrating a clean, user-controlled data export feature that maps directly onto CityRide Lite's requirement to export journeys and the end-of-day summary report to CSV files. [Transport for London](https://tfl.gov.uk/maps_/using-tfl-go)
- Splitwise Pro subscribers can back up all their data to JSON format, downloadable from the website showing how the same dataset can be exported in both a human-readable format (CSV) and a structured machine-readable format (JSON), which is exactly the dual-format approach CityRide Lite Part 2 uses: JSON for profiles and config, CSV for journey reports

**What it does poorly:**

- The app lacks options to export expense data in various formats users can only download CSV and not full formatted reports meaning there is no human-readable summary document that a user could share or print. CityRide Lite addresses this gap directly by exporting both a CSV report with line items and a separate human-readable text summary, giving riders two useful output formats.

**Key design ideas you could reuse:**

- The add/edit/delete transaction pattern with running totals where every change immediately updates the overall balance is the same pattern CityRide Lite needs for journey management: delete a journey, recalculate the daily total and cap status instantly.
- The dual export model (CSV for data, structured format for backup) maps directly onto CityRide Lite's file format rules: CSV for journeys and reports, JSON for profiles and config.
- The menu-driven navigation (groups, expenses, balances, export) where each function is clearly separated and accessible from a top-level menu is a strong model for CityRide Lite's menu structure: profiles, journeys, calculate, reports, save/exit, and admin.

**Screnshot:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Source%202.png)

### Gantt Chart

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Gant_chart_m1.png)

### Diary Entries

---

****(Bfore going further If you cant see some of the screenshot or they are ccrossing the pages please check the git hub link that I provided for you there I uploaded all screenshots full size so you can see everything. Thank you! ) ****

#### **20.03.2026** Project initiation

I started today by going through the assignment brief and highlighting the functional requirements for Part 2. The first thing I noticed was that this part is quite different from Part 1 becouse some of the features we built before, like filtering journeys by passenger type or cap status, etc... are not needed anymore. That actually means that I can not use the algorithms that I did for part 1 later although there is still similarities.

After that started writinf the purpose of the program. To get a better feel for how the system should work, I tried to think about it from two different angles, I put myself in the admin's position first, then the rider's. Doing it this way helped me understand what each role actually does, how the program should behave when it starts, and why the two menus need to be completely separate.

The system constraints part was a bit tricky at first. The problem was that some of the functional requirements felt like they belonged in the constraints section and the other way around, so the two were getting mixed up in my head. I ended up separating them myself before writing anything down, which made it much easier to organise my thoughts and then put everything into the markdown file.

By the end of the session I had the purpose written, the functional requirements analysed, and the system constraints done and separated clearly.

#### 21.03.2026 Creating IPO and Research

The first thing I did today was the IPO table. I started with the simpler and more obvious features things like rider menu choices, admin menu choices, and similar obvius inputs because I already had experience building an IPO table from Part 1. That made the whole process feel a lot more comfortable this time. I gradually extended it and ended up with around 26–27 features, which is a big jump from the first milestone 1 Part 1 where I only had about 8. Looking at that difference actually made me realise that I developed my analysing skills compared to when I started.

After finishing the IPO table I had a decision to make start the algorithm design or do the research first. I went with research because I felt that doing it after the design stage would not be really useful. The whole point of research is to inform my design decisions, so it made more sense to do it before algorithms.

I went through quite a lot of programs while looking for good sources. I started with simple ticket management apps for mobile phones and worked my way up to airline booking systems, but in the end I decided to keep TfL as one of my sources. I think it fits the project better than anything else I found. The difference this time compared to Part 1 is that I was looking at it from a completely different angle instead of focusing on fare calculation or something like that, I was looking specifically at how they handle profile-linked data for each rider and the save-before-exit pattern, both of which I will definitely use to use in my design.

For the second source I wanted to pick something new rather than reuse another source from Part 1. I chose Splitwise, a mobile application for splitting expenses. What stood out to me about it, was the menu-driven structure where every function is clearly separated and accessible from the top-level menu, which is exactly the kind of navigation design I want to follow in CityRide Lite Part 2. I added screenshots of both sources to the markdown file to complete the research section.

#### 22.03.2026 Algorithms and Class diagram

Today was definitely the most challenging day so far. I spent the whole session working on the algorithm design and the class diagram, and both of them turned out to be more complex than I expected.

For the flowcharts I ended up with 10 in total, which might seem like a lot, but I genuinely could not find a better way to cover everything clearly. I put a lot of effort into the decomposition I separated the admin and rider flows straight away because their roles and responsibilities in the program are completely different, so it made no sense to mix them together.

On the rider, I started with the main menu flowchart showing everything the rider can do in the program. Then I created a separate flowchart for profile management because that is a completely new feature that did not exist in Part 1. After that I went through all the other functions the rider needs to perform. Some of them had similarities with Part 1 so I did not go into too much detail there, but I still created the flowcharts to capture the small changes in logic that came with the new requirements.

For the admin I did not want to end up with another ten flowcharts, so I was more selective. I created the main admin menu flowchart and included the view config option within it because that one is straightforward enough not to need its own diagram. However, I did create a dedicated flowchart for managing the configuration settings because that part has more complex logic it involves validating entered values, writing data to file, and handling the overall config management flow, which I felt needed its own diagram to explain properly.

The class diagram was honestly the hardest part of the day. I knew going in that it would be bigger and more complex than what I did for Part 1, but it turned out to be even more complex than I had thought. I created all the classes with their attributes and methods without too much trouble, but when I started drawing the relationships I ran into a layout problem almost every class is connected to at least one other class in some way, and no matter how I arranged them I could not avoid lines crossing over each other. I spent quite a bit of time trying different layouts but could not fully solve it. In the end the best solution I could come up with was to colour-code the classes that had overlapping relationships, so that the tutor can still follow which class connects to which even where the lines cross. It is not perfect but I think it communicates the design clearly enough.

Once the diagrams were done I added all the screenshots flowcharts and the class diagram to the markdown file. At that point I noticed the Gantt chart was still missing, so I created it and took a screenshot that captures all the tasks with their timelines, making sure every milestone up to the final submission was visible.

---

#### Appendix B - Milestone 2:

# IY4113 Part 2 Milestone 3

| Assessment Details | Please Complete All Details                                 |
| ------------------ | ----------------------------------------------------------- |
| Group              | B                                                           |
| Module Title       | IY4113 Applied Software Engineering using Object-Orientated |
| Programming        | Java                                                        |
| Assessment Type    | Coursework                                                  |
| Module Tutor Name  | Jonathan Shore                                              |
| Student ID Number  | P0469808                                                    |
| Date of Submission | 06.04.2026                                                  |
| Word Count         | 5368                                                        |
| GitHub Link        | https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808   |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*

- [x] *Where I have used AI, I have cited and referenced appropriately.

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 2, at least 3)

------------------------------------------------------------------------------------------------------------------------------

#### Research 1:

**Title of research:** How to read and write JSON files using core Java

**Reference:** Attacomsian. (2019). *How to read and write JSON files using core Java*. Available at: [How to read and write JSON Files in Java](https://attacomsian.com/blog/java-read-write-json-files) [Accessed 04 April 2026]

**How does the research help with coding practise?**:

Part 2 requires saving rider profiles and system configuration to JSON files between sessions. Java has no built-in JSON library, so this source showed me how to handle JSON using only core Java building JSON strings with StringBuilder and writing them with Files.writeString(). This directly informed my JsonFileHandler class, which serialises rider profiles and config data without any external dependencies. The source also highlighted that special characters in string values (like quotes or backslashes) can break a JSON file if not escaped. I will add an escape() helper method in JsonFileHandler specifically because of this. 

**Key coding ideas you could reuse in your program:**

Use Files.writeString() and Files.readString() for clean, one-line file access with no streams to manage manually. Build JSON output field by field using StringBuilder so the format is readable and easy to debug. Always wrap file operations in try-catch, return null or false on failure, and print a specific error message so the user knows what went wrong.

**Screenshot of research:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%201.png)

------------------------------------------------------------------------------------------------------------------------------

#### Research 2:

**Title of research:** Reading and Writing CSVs in Java

**Reference (link):** Stack Abuse. (2019). *Reading and Writing CSVs in Java*. Available at: [Reading and Writing CSVs in Java](https://stackabuse.com/reading-and-writing-csvs-in-java/) [Accessed 04 April 2026]

**How does the research help with coding practise?:** 

Also requirements of Part 2 requires importing journeys from a CSV file and exporting journeys and summary reports to CSV. This source showed how to handle CSV files using only core Java BufferedReader for reading and FileWriter for writing with no external libraries needed. The most important thing I learned was that the header row must be read and skipped before the loop starts, otherwise the program tries to parse column names as journey data. I will apply this in readJourneys() with a reader.readLine() call before the loop begins. The source also explained that this approach breaks if field values contain commas. This is why I will try to keep the rider profile details separate from the journey CSV entirely the rider's name and passenger type are already stored in the JSON profile file, so duplicating them in every CSV row would be messy and harder to maintain if the rider's details ever changed. All write operations will use String.join() followed by writer.newLine(), and invalid rows during import are skipped with a warning rather than stopping the whole operation.

**Key coding ideas you could reuse in your program:**

Read CSV files line by line with BufferedReader.readLine() in a while loop until null is returned. Write each row as a comma-separated string using String.join() followed by writer.newLine(). Skip invalid rows with a warning message instead of stopping the whole import this way one bad row does not block all the others from loading.

**Screenshot of research:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%202.png)

---

#### Research 3:

**Title of research:** LocalDateTime Class in Java

**Reference:** Nkamphoa. (2026). *LocalDateTime Class in Java*. Available at: [LocalDateTime Class in Java](https://nkamphoa.com/localdatetime-class-in-java/) [Accessed 04 April 2026]

**How does the research help with coding practice:**

In Part 1 the Journey class used LocalDate to record when a journey was added just the date, with no time. Part 2 requirement specificaly says that each journey must include a date and time, so LocalDate was no longer sufficient. I needed to understand LocalDateTime and how to switch to it without breaking the existing journey logic. This source explained that LocalDateTime stores both date and time in a single object with the default format yyyy-MM-dd-mm:ss, and that it can be parsed from a string using LocalDateTime.parse() with a custom DateTimeFormatter for example DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"). This is what I will use in the Menu.readDateTime() method, where the user enters a journey time in the format 04/04/2026 08:30 and the input is parsed into a LocalDateTime object. The source also explained that LocalDateTime keeps toLocalDate() available, which let me keep the existing date-based logic in JourneyManager such as grouping journeys by day for the daily cap calculation without rewriting it. I simply call j.getDate() which internally calls dateTime.toLocalDate() on the stored LocalDateTime.

Key coding ideas you could reuse:

Use DateTimeFormatter.of Pattern ("dd/MM/yyyy HH:mm") to parse user-entered date and time strings into a LocalDateTime object define the formatter as a static final constant in the class so it is created once and reused. Call dateTime.toLocalDate() when you only need the date part of a LocalDateTime, which avoids having to store a separate LocalDate field. Catch DateTimeParseException when parsing user input and re-prompt with a clear format example.

**Screenshot:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Source%203.png)

------------------------------------------------------------------------------------------------------------------------------

### Program Code

---

Paste the current program code created so far. It does not have to be runnable code (document though if it does not work!)

------------------------------------------------------------------------------------------------------------------------------

*Program code goes here:*

```java
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CityRideLite {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice = -1;

        JourneyManager manager = new JourneyManager();
        SummaryReport summaryReport = new SummaryReport();

        do {
            System.out.println("\n=== CityRide Lite ===");
            System.out.println("1) Add journey");
            System.out.println("2) List journeys");
            System.out.println("3) Summary");
            System.out.println("4) Remove journey");
            System.out.println("0) Exit");
            System.out.print("Choose an option (0-4): ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("ERROR: Please enter a number.");
                continue;
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addJourneyUI(sc, manager);
                    break;

                case 2:
                    listJourneysUI(manager);
                    break;

                case 3:
                    summaryUI(sc, manager, summaryReport);
                    break;

                case 4:
                    removeJourneyUI(sc, manager);
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("ERROR: Please enter a number from 0 to 4.");
                    break;
            }

        } while (choice != 0);

        sc.close();
    }

    private static void addJourneyUI(Scanner sc, JourneyManager manager) {

        LocalDate date = readDateDayMonthYear(sc);
        int from = readIntInRange(sc, "From zone (1-5): ", 1, 5);
        int to = readIntInRange(sc, "To zone (1-5): ", 1, 5);

        CityRideDataset.TimeBand band = readTimeBand(sc);
        CityRideDataset.PassengerType type = readPassengerType(sc);

        boolean added = manager.addJourney(date, from, to, band, type);

        if (added) {
            System.out.println("Journey added.");
        } else {
            System.out.println("Journey not added");
        }
    }

    private static void listJourneysUI(JourneyManager manager) {

        List<Journey> list = manager.getJourneys();

        if (list.isEmpty()) {
            System.out.println("No journeys recorded.");
        } else {
            int i = 0;

            while (i < list.size()) {
                System.out.println(list.get(i));
                i++;
            }
        }
    }

    private static void summaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport) {

        LocalDate date = readDateDayMonthYear(sc);
        summaryReport.printSummary(manager, date);
    }

    private static void removeJourneyUI(Scanner sc, JourneyManager manager) {

        List<Journey> list = manager.getJourneys();

        if (list.isEmpty()) {
            System.out.println("No journeys to remove.");
        } else {

            listJourneysUI(manager);

            int id = readIntMin(sc, "Enter journey ID to remove (>=1): ", 1);

            boolean confirm = readYesNo(sc, "Confirm remove journey ID=" + id + "? (Y/N): ");

            if (!confirm) {
                System.out.println("Removal cancelled.");
            } else {
                boolean removed = manager.removeJourneyById(id);

                if (removed) {
                    System.out.println("Journey removed.");
                } else {
                    System.out.println("No journey found with ID=" + id);
                }
            }
        }
    }

    private static String readRequiredName(Scanner sc) {

        boolean valid = false;
        String name = "";

        while (!valid) {

            System.out.print("Enter passenger name (required): ");
            name = sc.nextLine().trim();

            if (name.length() == 0) {
                System.out.println("Name cannot be empty.");
            } else {
                valid = true;
            }
        }

        return name;
    }

    private static LocalDate readDateDayMonthYear(Scanner sc) {

        boolean valid = false;
        LocalDate date = LocalDate.now();

        while (!valid) {

            System.out.print("Enter date (DD-MM-YYYY): ");
            String s = sc.nextLine().trim();

            if (s.length() == 0) {
                System.out.println("Date is required.");
            } else {

                try {
                    date = LocalDate.parse(s, DATE_FORMAT);
                    valid = true;
                } catch (DateTimeParseException ex) {
                    System.out.println("Invalid date. Example: 22-02-2026");
                }
            }
        }

        return date;
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {

        boolean valid = false;
        int value = min;

        while (!valid) {
            System.out.print(prompt);

            String input = sc.nextLine().trim();

            if (!isDigits(input)) {
                System.out.println("Invalid input. Please enter a whole number.");
            } else {
                value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println("Out of range. Try again.");
                } else {
                    valid = true;
                }
            }
        }

        return value;
    }

    private static CityRideDataset.TimeBand readTimeBand(Scanner sc) {

        boolean valid = false;
        CityRideDataset.TimeBand band = CityRideDataset.TimeBand.PEAK;

        while (!valid) {
            System.out.print("Time band (P=PEAK, O=OFF_PEAK): ");
            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("P") || s.equals("PEAK")) {
                band = CityRideDataset.TimeBand.PEAK;
                valid = true;
            } else if (s.equals("O") || s.equals("OFF_PEAK")) {
                band = CityRideDataset.TimeBand.OFF_PEAK;
                valid = true;
            } else {
                System.out.println("Invalid input. Type P or O.");
            }
        }

        return band;
    }

    private static CityRideDataset.PassengerType readPassengerType(Scanner sc) {

        boolean valid = false;
        CityRideDataset.PassengerType type = CityRideDataset.PassengerType.ADULT;

        while (!valid) {
            System.out.print("Passenger type (ADULT/STUDENT/CHILD/SENIOR_CITIZEN): ");

            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("SENIOR")) {
                s = "SENIOR_CITIZEN";
            }

            if (s.equals("ADULT")) {
                type = CityRideDataset.PassengerType.ADULT;
                valid = true;
            } else if (s.equals("STUDENT")) {
                type = CityRideDataset.PassengerType.STUDENT;
                valid = true;
            } else if (s.equals("CHILD")) {
                type = CityRideDataset.PassengerType.CHILD;
                valid = true;
            } else if (s.equals("SENIOR_CITIZEN")) {
                type = CityRideDataset.PassengerType.SENIOR_CITIZEN;
                valid = true;
            } else {
                System.out.println("Invalid input. Try: ADULT, STUDENT, CHILD, SENIOR_CITIZEN.");
            }
        }

        return type;
    }

    private static boolean isDigits(String text) {

        boolean ok = true;

        if (text.length() == 0) {
            ok = false;
        } else {
            int i = 0;
            while (i < text.length() && ok) {
                char c = text.charAt(i);

                if (!Character.isDigit(c)) {
                    ok = false;
                }

                i++;
            }
        }

        return ok;
    }

    private static int readIntMin(Scanner sc, String prompt, int min) {

        boolean isValid = false;
        int value = min;

        while (!isValid) {

            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (!isDigits(input)) {
                System.out.println("Invalid input. Please enter a whole number.");
            } else {

                value = Integer.parseInt(input);

                if (value < min) {
                    System.out.println("Out of range. Input should be positive whole number. " + min);
                } else {
                    isValid = true;
                }
            }
        }

        return value;
    }

    private static boolean readYesNo(Scanner sc, String prompt) {

        boolean valid = false;
        boolean answer = false;

        while (!valid) {

            System.out.print(prompt);
            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("Y") || s.equals("YES")) {
                answer = true;
                valid = true;
            } else if (s.equals("N") || s.equals("NO")) {
                answer = false;
                valid = true;
            } else {
                System.out.println("Invalid input. Type Y or N.");
            }
        }

        return answer;
    }
    private static void startApp(Scanner sc) {
    }

    private static int readRoleChoice(Scanner sc) {
        return 0;
    }

    private static void openRiderFlow(Scanner sc,
                                      JourneyManager manager,
                                      SummaryReport summaryReport,
                                      //RiderMenu riderMenu,
                                      ProfileManager profileManager,
                                      ReportExporter reportExporter,
                                      CsvFileHandler csvFileHandler,
                                      JsonFileHandler jsonFileHandler) {
    }

    private static void openAdminFlow(Scanner sc,
                                      AdminMenu adminMenu,
                                      ConfigManager configManager) {
    }

}

class FareCalculator {

    public BigDecimal discountedFare(int fromZone, int toZone,
                                     CityRideDataset.TimeBand band,
                                     CityRideDataset.PassengerType type) {

        BigDecimal result = new BigDecimal("0.00");


        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, band);


        if (baseFare != null) {


            BigDecimal discountRate = CityRideDataset.DISCOUNT_RATE.get(type);


            BigDecimal discountAmount = baseFare.multiply(discountRate);


            BigDecimal discounted = baseFare.subtract(discountAmount);

            result = money(discounted);
        }

        return result;
    }

    public BigDecimal applyCap(BigDecimal runningTotal, BigDecimal discountedFare,
                               CityRideDataset.PassengerType type) {

        BigDecimal result;

        BigDecimal cap = CityRideDataset.DAILY_CAP.get(type);


        if (runningTotal.compareTo(cap) >= 0) {
            result = money("0.00");
        } else {

            BigDecimal remaining = cap.subtract(runningTotal);


            if (discountedFare.compareTo(remaining) > 0) {
                result = money(remaining);
            } else {
                result = money(discountedFare);
            }
        }

        return result;
    }


    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(String s) {
        BigDecimal value = new BigDecimal(s);
        return money(value);
    }
}

class JourneyManager {

    private List<Journey> journeys;
    private FareCalculator calc;

    private int nextID;

    public JourneyManager() {
        journeys = new ArrayList<>();
        calc = new FareCalculator();
        nextID = 1;
    }

    public Journey findJourneyById(int id) {

        Journey foundJourney = null;

        int i = 0;
        while (i < journeys.size() && foundJourney == null) {

            Journey currentJourney = journeys.get(i);

            if (currentJourney.getId() == id) {
                foundJourney = currentJourney;
            }

            i++;
        }

        return foundJourney;
    }

    public void recalculateChargedFaresForDay(LocalDate date) {
    }

    // return true if journey was added, false if not added
    public boolean addJourney(LocalDate date, int fromZone, int toZone, CityRideDataset.TimeBand band, CityRideDataset.PassengerType type) {

        boolean added = false;

        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, band);

        if (baseFare != null) {

            BigDecimal discountedFare = calc.discountedFare(fromZone, toZone, band, type);

            BigDecimal runningTotal = getTotalChargedForDay(date, type);

            BigDecimal chargedFare = calc.applyCap(runningTotal, discountedFare, type);

            int id = nextID; //generating a unique id for every journey here!
            nextID++;

            Journey newJourney = new Journey(date, id, fromZone, toZone, band, type,
                    baseFare, discountedFare, chargedFare); //adding the generated id to the journey

            journeys.add(newJourney);

            added = true;
        }


        return added;
    }

    public boolean removeJourneyById(int id) {
        // Removing the journey by id not by index as I did before.

        boolean removed = false;

        int i = 0;
        while (i < journeys.size() && !removed) {

            Journey j = journeys.get(i);

            if (j.getId() == id) {
                journeys.remove(i);
                removed = true;
            } else {
                i++;
            }
        }

        return removed;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                total = total.add(j.getChargedFare());
            }

            i++;
        }

        return total;
    }

    public BigDecimal getTotalChargedForDay(LocalDate date, CityRideDataset.PassengerType type) {

        BigDecimal total = new BigDecimal("0.00");

        int i = 0;
        while (i < journeys.size()) {

            Journey j = journeys.get(i);

            if (j.getDate().equals(date)) {
                if (j.getType() == type) {
                    total = total.add(j.getChargedFare());
                }
            }

            i++;
        }

        return total;
    }
}

class RiderProfile {

    enum PaymentOption {
        CARD,
        CASH
    }

    private String name;
    private CityRideDataset.PassengerType passengerType;
    private PaymentOption defaultPaymentOption;

    public RiderProfile() {
    }

    public RiderProfile(String name,
                        CityRideDataset.PassengerType passengerType,
                        PaymentOption defaultPaymentOption) {
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPaymentOption = defaultPaymentOption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CityRideDataset.PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(CityRideDataset.PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public PaymentOption getDefaultPaymentOption() {
        return defaultPaymentOption;
    }

    public void setDefaultPaymentOption(PaymentOption defaultPaymentOption) {
        this.defaultPaymentOption = defaultPaymentOption;
    }
}

class ProfileManager {

    private RiderProfile currentProfile;

    public RiderProfile createProfile(String name,
                                      CityRideDataset.PassengerType passengerType,
                                      RiderProfile.PaymentOption paymentOption) {

        RiderProfile profile = new RiderProfile(name, passengerType, paymentOption);
        currentProfile = profile;

        return profile;
    }

    public RiderProfile loadProfile(String filePath, JsonFileHandler jsonFileHandler) {
        return null;
    }

    public boolean saveProfile(String filePath, JsonFileHandler jsonFileHandler) {
        return false;
    }

    public RiderProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(RiderProfile profile) {
        currentProfile = profile;
    }

    public boolean hasCurrentProfile() {
        boolean hasProfile = false;

        if (currentProfile != null) {
            hasProfile = true;
        }

        return hasProfile;
    }
}

class RiderMenu {

    public void showMenu(Scanner sc,
                         JourneyManager manager,
                         SummaryReport summaryReport,
                         ProfileManager profileManager,
                         ReportExporter reportExporter,
                         CsvFileHandler csvFileHandler,
                         JsonFileHandler jsonFileHandler) {
    }

    private void createProfileUI(Scanner sc, ProfileManager profileManager) {
    }

    private void loadProfileUI(Scanner sc, ProfileManager profileManager, JsonFileHandler jsonFileHandler) {
    }

    private void saveProfileUI(ProfileManager profileManager, JsonFileHandler jsonFileHandler) {
    }

    private void addJourneyUI(Scanner sc, JourneyManager manager, ProfileManager profileManager) {
    }

    private void editJourneyUI(Scanner sc, JourneyManager manager, ProfileManager profileManager) {
    }

    private void deleteJourneyUI(Scanner sc, JourneyManager manager) {
    }

    private void listJourneysUI(JourneyManager manager) {
    }

    private void showSummaryUI(Scanner sc, JourneyManager manager, SummaryReport summaryReport) {
    }

    private void importJourneysUI(Scanner sc, JourneyManager manager, CsvFileHandler csvFileHandler) {
    }

    private void exportJourneysUI(Scanner sc, JourneyManager manager, CsvFileHandler csvFileHandler) {
    }

    private void exportSummaryUI(Scanner sc,
                                 JourneyManager manager,
                                 SummaryReport summaryReport,
                                 ReportExporter reportExporter,
                                 ProfileManager profileManager) {
    }

    private void saveCurrentDayStateUI(ProfileManager profileManager,
                                       JourneyManager manager,
                                       JsonFileHandler jsonFileHandler) {
    }
}

class ConfigManager {

    public SystemConfig loadConfig(JsonFileHandler jsonFileHandler) {
        return null;
    }

    public SystemConfig createDefaultConfig() {
        return null;
    }

    public boolean saveConfig(SystemConfig config, JsonFileHandler jsonFileHandler) {
        return false;
    }

    public SystemConfig getCurrentConfig() {
        return null;
    }

    public void setCurrentConfig(SystemConfig config) {
    }

    public void updateBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
    }

    public void updateDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
    }

    public void updateDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
    }

    public void updatePeakWindow(String peakStart, String peakEnd) {
    }
}

class CsvFileHandler {

    public List<Journey> importJourneys(String filePath) {
        return new ArrayList<>();
    }

    public boolean exportJourneys(String filePath, List<Journey> journeys) {
        return false;
    }

    public boolean exportLineItems(String filePath, List<Journey> journeys) {
        return false;
    }
}

class JsonFileHandler {

    public RiderProfile loadProfile(String filePath) {
        return null;
    }

    public boolean saveProfile(String filePath, RiderProfile profile) {
        return false;
    }

    public SystemConfig loadConfig(String filePath) {
        return null;
    }

    public boolean saveConfig(String filePath, SystemConfig config) {
        return false;
    }
}

class SystemConfig {

    public BigDecimal getBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band) {
        return null;
    }

    public void setBaseFare(int fromZone, int toZone, CityRideDataset.TimeBand band, BigDecimal fare) {
    }

    public BigDecimal getDiscount(CityRideDataset.PassengerType type) {
        return null;
    }

    public void setDiscount(CityRideDataset.PassengerType type, BigDecimal discount) {
    }

    public BigDecimal getDailyCap(CityRideDataset.PassengerType type) {
        return null;
    }

    public void setDailyCap(CityRideDataset.PassengerType type, BigDecimal cap) {
    }

    public String getPeakStart() {
        return null;
    }

    public String getPeakEnd() {
        return null;
    }

    public void setPeakWindow(String peakStart, String peakEnd) {
    }
}

class ReportExporter {

    public boolean exportSummaryAsText(String filePath,
                                       String riderName,
                                       LocalDate date,
                                       SummaryReport summaryReport,
                                       JourneyManager manager) {

        boolean success = false;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            writer.write("Rider: " + riderName);
            writer.newLine();
            writer.newLine();
            writer.write(summaryReport.buildSummaryText(manager, date));
            writer.close();

            success = true;

        } catch (IOException ex) {
            System.out.println("Error writing summary text file.");
        }

        return success;
    }
}

class AdminMenu {

    public void showMenu(Scanner sc, ConfigManager configManager) {
    }

    private boolean loginUI(Scanner sc) {
        return false;
    }

    private void viewConfigUI(ConfigManager configManager) {
    }

    private void updateBaseFareUI(Scanner sc, ConfigManager configManager) {
    }

    private void updateDiscountUI(Scanner sc, ConfigManager configManager) {
    }

    private void updateDailyCapUI(Scanner sc, ConfigManager configManager) {
    }

    private void updatePeakWindowUI(Scanner sc, ConfigManager configManager) {
    }
}

class SummaryReport {

    public void printSummary(JourneyManager manager, LocalDate date) {

        List<Journey> list = manager.getJourneys();

        int totalJourneys = 0;
        BigDecimal totalCharged = new BigDecimal("0.00");

        int mostExpensiveId = -1;
        BigDecimal mostExpensiveFare = new BigDecimal("0.00");

        int peakCount = 0;
        int offPeakCount = 0;

        int[][] zonePairCounts = new int[6][6]; // 1..5 used

        int i = 0;
        while (i < list.size()) {

            Journey j = list.get(i);

            if (j.getDate().equals(date)) {

                totalJourneys++;

                BigDecimal charged = j.getChargedFare();
                totalCharged = totalCharged.add(charged);

                if (mostExpensiveId == -1 || charged.compareTo(mostExpensiveFare) > 0) {
                    mostExpensiveFare = charged;
                    mostExpensiveId = j.getId();
                }

                if (j.getBand() == CityRideDataset.TimeBand.PEAK) {
                    peakCount++;
                } else {
                    offPeakCount++;
                }

                int from = j.getFromZone();
                int to = j.getToZone();
                if (from >= 1 && from <= 5 && to >= 1 && to <= 5) {
                    zonePairCounts[from][to] = zonePairCounts[from][to] + 1;
                }
            }

            i++;
        }

        System.out.println("\n Daily Summary (" + date + ")");
        System.out.println("Total number of journeys: " + totalJourneys);
        System.out.println("Total cost charged: " + totalCharged.setScale(2, RoundingMode.HALF_UP));

        BigDecimal average = new BigDecimal("0.00");
        if (totalJourneys > 0) {
            average = totalCharged.divide(new BigDecimal(totalJourneys), 2, RoundingMode.HALF_UP);
        }
        System.out.println("Average cost per journey: " + average);

        if (mostExpensiveId == -1) {
            System.out.println("Most expensive journey: ");
        } else {
            System.out.println("Most expensive journey: ID=" + mostExpensiveId + " | charged=" +
                    mostExpensiveFare.setScale(2, RoundingMode.HALF_UP));
        }

        System.out.println("\n--- Category Counts (" + date + ") ---");
        System.out.println("Peak journeys: " + peakCount);
        System.out.println("Off-peak journeys: " + offPeakCount);

        System.out.println("\nZone pair counts:");
        int from = 1;
        while (from <= 5) {
            int to = 1;
            while (to <= 5) {
                int c = zonePairCounts[from][to];
                if (c > 0) {
                    System.out.println(from + "->" + to + ": " + c);
                }
                to++;
            }
            from++;
        }
    }

    public BigDecimal calculateSavings(JourneyManager manager, LocalDate date) {

        BigDecimal savings = new BigDecimal("0.00");

        List<Journey> list = manager.getJourneys();

        int i = 0;
        while (i < list.size()) {

            Journey currentJourney = list.get(i);

            if (currentJourney.getDate().equals(date)) {

                BigDecimal discountedFare = currentJourney.getDiscountedFare();
                BigDecimal chargedFare = currentJourney.getChargedFare();

                BigDecimal journeySaving = discountedFare.subtract(chargedFare);
                savings = savings.add(journeySaving);
            }

            i++;
        }

        return savings.setScale(2, RoundingMode.HALF_UP);
    }

    public String buildSummaryText(JourneyManager manager, LocalDate date) {

        List<Journey> list = manager.getJourneys();

        int totalJourneys = 0;
        BigDecimal totalCharged = new BigDecimal("0.00");
        int mostExpensiveId = -1;
        BigDecimal mostExpensiveFare = new BigDecimal("0.00");
        int peakCount = 0;
        int offPeakCount = 0;

        int i = 0;
        while (i < list.size()) {

            Journey currentJourney = list.get(i);

            if (currentJourney.getDate().equals(date)) {

                totalJourneys++;
                totalCharged = totalCharged.add(currentJourney.getChargedFare());

                if (mostExpensiveId == -1 ||
                        currentJourney.getChargedFare().compareTo(mostExpensiveFare) > 0) {
                    mostExpensiveFare = currentJourney.getChargedFare();
                    mostExpensiveId = currentJourney.getId();
                }

                if (currentJourney.getBand() == CityRideDataset.TimeBand.PEAK) {
                    peakCount++;
                } else {
                    offPeakCount++;
                }
            }

            i++;
        }

        BigDecimal average = new BigDecimal("0.00");
        if (totalJourneys > 0) {
            average = totalCharged.divide(new BigDecimal(totalJourneys), 2, RoundingMode.HALF_UP);
        }

        BigDecimal savings = calculateSavings(manager, date);

        String text = "";
        text = text + "CityRide Lite Daily Summary\n";
        text = text + "Date: " + date + "\n";
        text = text + "Total journeys: " + totalJourneys + "\n";
        text = text + "Total charged: " + totalCharged.setScale(2, RoundingMode.HALF_UP) + "\n";
        text = text + "Average cost per journey: " + average + "\n";

        if (mostExpensiveId == -1) {
            text = text + "Most expensive journey: none\n";
        } else {
            text = text + "Most expensive journey: ID=" + mostExpensiveId +
                    " | charged=" + mostExpensiveFare.setScale(2, RoundingMode.HALF_UP) + "\n";
        }

        text = text + "Savings from cap: " + savings + "\n";
        text = text + "Peak journeys: " + peakCount + "\n";
        text = text + "Off-peak journeys: " + offPeakCount + "\n";

        return text;
    }
}

class Journey {
    private LocalDate date;
    private int fromZone;
    private int toZone;
    private int zonesCrossed;

    private CityRideDataset.TimeBand band;
    private CityRideDataset.PassengerType type;

    private BigDecimal baseFare;
    private BigDecimal discountedFare;
    private BigDecimal discountApplied;
    private BigDecimal chargedFare;

    private int id;

    public Journey(LocalDate date, int id, int fromZone, int toZone,
                   CityRideDataset.TimeBand band,
                   CityRideDataset.PassengerType type,
                   BigDecimal baseFare, BigDecimal discountedFare, BigDecimal chargedFare) {

        this.id = id;
        this.date = date;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.zonesCrossed = Math.abs(toZone - fromZone) + 1;

        this.band = band;
        this.type = type;

        this.baseFare = baseFare;
        this.discountedFare = discountedFare;
        this.discountApplied = baseFare.subtract(discountedFare).setScale(2, RoundingMode.HALF_UP);

        this.chargedFare = chargedFare;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public CityRideDataset.PassengerType getType() {
        return type;
    }

    public CityRideDataset.TimeBand getBand() {
        return band;
    }

    public int getFromZone() {
        return fromZone;
    }

    public int getToZone() {
        return toZone;
    }

    public int getZonesCrossed() {
        return zonesCrossed;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getDiscountedFare() {
        return discountedFare;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public BigDecimal getChargedFare() {
        return chargedFare;
    }

    public String toString() {
        return "ID=" + id + " | " + date + " | " + type + " | " + band + " | "
                + fromZone + "->" + toZone
                + " | zonesCrossed=" + zonesCrossed
                + " | base=" + baseFare
                + " | discount=" + discountApplied
                + " | discounted=" + discountedFare
                + " | charged=" + chargedFare;
    }
    public void setDate(LocalDate date) {
    }

    public void setFromZone(int fromZone) {
    }

    public void setToZone(int toZone) {
    }

    public void setBand(CityRideDataset.TimeBand band) {
    }

    public void setType(CityRideDataset.PassengerType type) {
    }

    public void setChargedFare(BigDecimal chargedFare) {
    }
}

final class CityRideDataset {

    private CityRideDataset() {
    }

    public static final int MIN_ZONE = 1;
    public static final int MAX_ZONE = 5;

    public enum TimeBand {
        PEAK,
        OFF_PEAK
    }

    public enum PassengerType {
        ADULT,
        STUDENT,
        CHILD,
        SENIOR_CITIZEN
    }

    public static final Map<PassengerType, BigDecimal> DISCOUNT_RATE = Map.of(
            PassengerType.ADULT, new BigDecimal("0.00"),
            PassengerType.STUDENT, new BigDecimal("0.25"),
            PassengerType.CHILD, new BigDecimal("0.50"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("0.30")
    );

    public static final Map<PassengerType, BigDecimal> DAILY_CAP = Map.of(
            PassengerType.ADULT, new BigDecimal("8.00"),
            PassengerType.STUDENT, new BigDecimal("6.00"),
            PassengerType.CHILD, new BigDecimal("4.00"),
            PassengerType.SENIOR_CITIZEN, new BigDecimal("7.00")
    );

    public static final Map<String, BigDecimal> BASE_FARE = buildBaseFare();

    public static BigDecimal getBaseFare(int fromZone, int toZone, TimeBand timeBand) {
        return BASE_FARE.get(key(fromZone, toZone, timeBand));
    }

    public static String key(int fromZone, int toZone, TimeBand timeBand) {
        return fromZone + "-" + toZone + "-" + timeBand.name();
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<String, BigDecimal> buildBaseFare() {
        Map<String, BigDecimal> m = new HashMap<>();

        // Peak fares
        put(m, 1, 1, TimeBand.PEAK, "2.50");
        put(m, 1, 2, TimeBand.PEAK, "3.20");
        put(m, 1, 3, TimeBand.PEAK, "3.80");
        put(m, 1, 4, TimeBand.PEAK, "4.40");
        put(m, 1, 5, TimeBand.PEAK, "5.00");

        put(m, 2, 1, TimeBand.PEAK, "3.20");
        put(m, 2, 2, TimeBand.PEAK, "2.30");
        put(m, 2, 3, TimeBand.PEAK, "3.10");
        put(m, 2, 4, TimeBand.PEAK, "3.80");
        put(m, 2, 5, TimeBand.PEAK, "4.50");

        put(m, 3, 1, TimeBand.PEAK, "3.80");
        put(m, 3, 2, TimeBand.PEAK, "3.10");
        put(m, 3, 3, TimeBand.PEAK, "2.10");
        put(m, 3, 4, TimeBand.PEAK, "3.00");
        put(m, 3, 5, TimeBand.PEAK, "3.70");

        put(m, 4, 1, TimeBand.PEAK, "4.40");
        put(m, 4, 2, TimeBand.PEAK, "3.80");
        put(m, 4, 3, TimeBand.PEAK, "3.00");
        put(m, 4, 4, TimeBand.PEAK, "2.00");
        put(m, 4, 5, TimeBand.PEAK, "2.90");

        put(m, 5, 1, TimeBand.PEAK, "5.00");
        put(m, 5, 2, TimeBand.PEAK, "4.50");
        put(m, 5, 3, TimeBand.PEAK, "3.70");
        put(m, 5, 4, TimeBand.PEAK, "2.90");
        put(m, 5, 5, TimeBand.PEAK, "1.90");

        // Off-peak fares
        put(m, 1, 1, TimeBand.OFF_PEAK, "2.00");
        put(m, 1, 2, TimeBand.OFF_PEAK, "2.70");
        put(m, 1, 3, TimeBand.OFF_PEAK, "3.20");
        put(m, 1, 4, TimeBand.OFF_PEAK, "3.70");
        put(m, 1, 5, TimeBand.OFF_PEAK, "4.20");

        put(m, 2, 1, TimeBand.OFF_PEAK, "2.70");
        put(m, 2, 2, TimeBand.OFF_PEAK, "1.90");
        put(m, 2, 3, TimeBand.OFF_PEAK, "2.60");
        put(m, 2, 4, TimeBand.OFF_PEAK, "3.20");
        put(m, 2, 5, TimeBand.OFF_PEAK, "3.80");

        put(m, 3, 1, TimeBand.OFF_PEAK, "3.20");
        put(m, 3, 2, TimeBand.OFF_PEAK, "2.60");
        put(m, 3, 3, TimeBand.OFF_PEAK, "1.70");
        put(m, 3, 4, TimeBand.OFF_PEAK, "2.50");
        put(m, 3, 5, TimeBand.OFF_PEAK, "3.10");

        put(m, 4, 1, TimeBand.OFF_PEAK, "3.70");
        put(m, 4, 2, TimeBand.OFF_PEAK, "3.20");
        put(m, 4, 3, TimeBand.OFF_PEAK, "2.50");
        put(m, 4, 4, TimeBand.OFF_PEAK, "1.60");
        put(m, 4, 5, TimeBand.OFF_PEAK, "2.40");

        put(m, 5, 1, TimeBand.OFF_PEAK, "4.20");
        put(m, 5, 2, TimeBand.OFF_PEAK, "3.80");
        put(m, 5, 3, TimeBand.OFF_PEAK, "3.10");
        put(m, 5, 4, TimeBand.OFF_PEAK, "2.40");
        put(m, 5, 5, TimeBand.OFF_PEAK, "1.50");

        return Map.copyOf(m);
    }

    private static void put(Map<String, BigDecimal> m, int from, int to, TimeBand band, String amount) {
        m.put(key(from, to, band), money(amount));
    }
}
```

---

### Updated Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Mileston%202/Updated%20gant%20char%202.png)

------------------------------------------------------------------------------------------------------------------------------

### Diary Entries (at least 4)

------------------------------------------------------------------------------------------------------------------------------

**(I have sent you coloborators invitation for the git hub repository becouse I have created new one for the part 2. Please accept it and the code commits history can be seen in the git hub in src folder. Thank you!)**

#### **03.04.2026:  Adapting Part 1 code to Part 2.**

I started today by going back through my Part 1 code and comparing it properly with the Part 2 requirements. The first thing I realised was that I could not just reuse everything as it was, because even though this is the same CityRide Lite program, Part 2 has quite a lot of new features that need different logic and methods. In Part 1, the main focus was on adding journeys, fare calculation, summaries, and similar core logic, but in Part 2 the system is supposed to become more realistic by adding rider profiles, file handling, and a separate admin side.

Because of that, I decided to adapt the Part 1 code into Part 2 instead of starting again from zero. The new requirements need different logic, and some of the methods from Part 1 do not really belong in the same places anymore, so I had to rethink the structure and decide which class each method should belong to. At the beginning I also thought about separating everything into different Java files, but it quickly became a headache to move parts around while I was still in the planning stage. In the end I decided to keep everything together for now, because it makes this stage easier to manage. What I mainly did today was create a draft skeleton of the methods, place them into the most suitable classes, and name them clearly, but I did not  implement  ane new Part 2 methods yet.

Another difficulty today was deciding what should stay from Part 1 and what should be removed. Some methods looked useful at first, but when I compared them properly to the new requirements, I realised that they either needed different logic or were no longer suitable in their current form. Some classes already had a useful structure, but other parts clearly needed to be expanded. By the end of the session I had adapted the old code into a more suitable draft for Part 2 and created a clearer starting point for the new features.

#### **04.04.2026: Conducting the research.**

Today I focused on research to support the coding side of the project. I did not want to start writing the file-handling parts blindly because Part 2 requires JSON for profiles and config, CSV for journeys and reports, and also date and time handling for journeys. Because of that I decided to look for sources that were directly useful for the actual coding problems which I will face.

The first source I researched was JSON in core Java. This was important because the rider profile and the configuration both need to be saved between sessions, but Java does not give simple built-in way to do that and have idea of how the text is written. After that I looked at CSV reading and writing, because importing journeys and exporting reports are requirements in Part 2. I also researched LocalDateTime because Part 2 requires date and time together, while my Part 1 logic was originally had LocalDate only.

What I found today will be helpfull later becouse these researches  were not just random reading. Each source gave me something specific that I could reuse in the program. For example, the CSV research helped me think about skipping the header row and keeping the logic simple, while the LocalDateTime research helped me think about how to add time without destroying the existing date-based fare logic. By the end of the session I had three relevant sources, added screenshots for them, and linked each one to a coding decision I plan to use in the program.

#### 05.04.2026: Refining the draft structure and preparing the first methods

Today I continued from the skeleton draft I made earlier and spent more time refining the overall code structure before implementing more of the Part 2 features. At this stage I still felt that jumping straight into the harder parts like full JSON and CSV handling would not be the best idea, because too many other parts of the program were still only placeholders. Because of that I decided to focus first on making the draft more organised and preparing the easier methods that could be added without breaking the existing Part 1 logic.

A lot of today was spent checking whether the responsibilities of the classes made sense. I went through classes like JourneyManager, SummaryReport, ProfileManager, ReportExporter and the menu classes, and thought more carefully about which methods should actually belong to each one. That part took longer than I expected because some methods looked simple at first, but if they were placed in the wrong class it would make the code more confusing later.

I also started identifying which empty methods from the draft would be the easiest ones to implement first. My aim was to identify what classes have dependencies on each other, and then choose a few smaller methods that would be easiest to implment. This felt like a better approach than rushing into the harder features and ending up with incomplete or messy code. By the end of the session I had a more stable draft structure, clearer method placement, and a better idea of which functions I could begin implementing first.

#### **06.04.2026: Started implementing some functions.**

Today I moved on from structure and planning into actual implementation. Because I was close to the Milestone 2 deadline, I made a  decision to focus on just two methods: buildSummaryText() in SummaryReport and exportSummaryAsText() in ReportExporter. I thought this was the better approach because it is more useful to show a small number of finished and understandable methods than a larger amount of unfinished code that I would not be able to explain properly.

The two methods I worked on today were both connected to the reporting side of Part 2. In SummaryReport, I implemented buildSummaryText(), which creates a readable daily summary using the journey data already stored in the program. It includes things like the total number of journeys, total charged, average cost, most expensive journey, savings from the cap, and peak/off-peak counts. After that, in ReportExporter, I implemented exportSummaryAsText(), which takes that summary text and writes it into a text file. I chose these methods because they were simple enough to complete in the time I had left, but they still match the Part 2 requirement for human-readable report output.

One issue I ran into today was that after adding the new methods, I still had some of the original empty draft methods left in the code. This caused duplicate method errors, so I had to go back through the classes and remove the unused stub versions while keeping only the implemented ones. Even though this was only a small problem, it showed me that working from a draft skeleton can create its own mistakes.

------------------------------------------------------------------------------------------------------------------------------
