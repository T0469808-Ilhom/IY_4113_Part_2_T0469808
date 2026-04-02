# IY4113 Milestone 1 Part 2

| Assessment Details | Please Complete All Details                                             |
| ------------------ | ----------------------------------------------------------------------- |
| Group              | B                                                                       |
| Module Title       | IY4113 Applied Software Engineering using Object-Orientated Programming |
| Assessment Type    | Coursework                                                              |
| Module Tutor Name  | Jonathan Shore                                                          |
| Student ID Number  | P0469808                                                                |
| Date of Submission | 22.03.2026                                                              |
| Word Count         | 3810                                                                    |
| GitHub Link        | https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808               |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*

- [x] *Where I have used AI, I have cited and referenced appropriately.

------------------------------------------------------------------------------------------------------------------------------

### Purpose of the Program

------------------------------------------------------------------------------------------------------------------------------

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

------------------------------------------------------------------------------------------------------------------------------

### Input Process Output Table

------------------------------------------------------------------------------------------------------------------------------

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

------------------------------------------------------------------------------------------------------------------------------

### Algorithm Design

---

#### Flowcharts:

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Flowchart_1.jpg)

#### 

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

------------------------------------------------------------------------------------------------------------------------------

### Research

---

#### Source 1

**Name of program:** TfL Contactless and Oyster Account

**Reference:** Transport for London. (2025). *Contactless and Oyster account*. Available at: [Transport for London](https://tfl.gov.uk/fares/contactless-and-oyster-account) [Accessed 21 March 2026]

**What it does well:**

- Once logged in, users can check their balance, view journey history, manage auto top-up settings, and update their personal details demonstrating how a real transport system links a rider's name, payment preferences, and journey records under a single profile, directly mirroring CityRide Lite Part 2's rider profile structure (name, passenger type, default payment option) saved and loaded between sessions.
- Journey history displays each trip with its date, fare charged, zones, and cap status  and users can select a custom date range to filter their records showing how per-journey cost data and daily summaries can be presented clearly to the rider, which maps directly onto CityRide Lite's requirements to view per-journey costs, running totals, and the end-of-day summary report.
- The latest TfL Go update allows customers to easily access their contactless or Oyster account and view their journey history directly in the app, as well as check and apply for refunds for incomplete journeys demonstrating how editing and correcting journey records within a profile-based system should work, which informs CityRide Lite's edit and delete journey features with totals recalculation.

**What it does poorly:**

- Oyster journey history is only stored for up to eight weeks after which it is permanently deleted meaning riders cannot access older records for expense tracking or auditing purposes. CityRide Lite avoids this limitation by saving the rider's full journey data locally to a JSON file, giving the rider permanent offline access to their own records

**Key design ideas you could reuse:**

- The profile-linked journey history model where a logged-in rider sees only their own journeys, costs, and cap status  is the exact model for CityRide Lite's rider session: load a profile, add journeys under it, and view a personalised summary.
- The date-grouped journey display (journeys grouped by day with totals shown beneath each group) is a strong model for CityRide Lite's end-of-day summary layout.
- The save-on-exit pattern  where the system saves your card state and session so you can continue next time directly informs CityRide Lite's requirement to offer to save the rider's profile and journeys when the program exits.

**Screenshot:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Source%201.png)

#### Source 2

**Name of program:** Splitwise

**Reference:** Splitwise Inc. (2025). *Splitwise — split expenses with friends* [Web and mobile application]. Available at: [https://www.splitwise.com](https://www.splitwise.com) [Accessed 21 March 2026]

**What it does well:**

- Splitwise keeps a running total over time per user, records all individual transactions with amounts and categories, and lets users add, edit, or delete any expense  with a full edit history showing every change made  directly mirroring CityRide Lite Part 2's requirements to add, edit, and delete journeys with totals recalculated after every change. [Top Tip London](https://www.toptiplondon.com/transport/getting-around-london/london-journey-planner)
- Users can export all their expense data to a CSV file from the web interface with a single click demonstrating a clean, user-controlled data export feature that maps directly onto CityRide Lite's requirement to export journeys and the end-of-day summary report to CSV files. [Transport for London](https://tfl.gov.uk/maps_/using-tfl-go)
- Splitwise Pro subscribers can back up all their data to JSON format, downloadable from the website  showing how the same dataset can be exported in both a human-readable format (CSV) and a structured machine-readable format (JSON), which is exactly the dual-format approach CityRide Lite Part 2 uses: JSON for profiles and config, CSV for journey reports

**What it does poorly:**

- The app lacks options to export expense data in various formats users can only download CSV and not full formatted reports  meaning there is no human-readable summary document that a user could share or print. CityRide Lite addresses this gap directly by exporting both a CSV report with line items and a separate human-readable text summary, giving riders two useful output formats.

**Key design ideas you could reuse:**

- The add/edit/delete transaction pattern with running totals  where every change immediately updates the overall balance  is the same pattern CityRide Lite needs for journey management: delete a journey, recalculate the daily total and cap status instantly.
- The dual export model (CSV for data, structured format for backup) maps directly onto CityRide Lite's file format rules: CSV for journeys and reports, JSON for profiles and config.
- The menu-driven navigation (groups, expenses, balances, export)  where each function is clearly separated and accessible from a top-level menu  is a strong model for CityRide Lite's menu structure: profiles, journeys, calculate, reports, save/exit, and admin.

**Screnshot:**

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Source%202.png)

------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------------------------------------------------

### Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![](https://github.com/T0469808-Ilhom/IY_4113_Part_2_T0469808/blob/master/Milestone1/Gant_chart_m1.png)

------------------------------------------------------------------------------------------------------------------------------

### Diary Entries

------------------------------------------------------------------------------------------------------------------------------ 



****(Bfore going further If you cant see some of the screenshot or they are ccrossing the pages please check the git hub link that I provided for  you there I uploaded all screenshots full size so you can see everything. Thank you! ) ****

#### **20.03.2026** Project initiation

I started today by going through the assignment brief  and highlighting the functional requirements for Part 2. The first thing I noticed was that this part is quite different from Part 1 becouse some of the features we built before, like filtering journeys by passenger type or cap status, etc... are not needed anymore. That actually means that I can not use the algorithms that I did for part 1 later although there is still similarities.

After that started  writinf the purpose of the program. To get a better feel for how the system should work, I tried to think about it from two different angles, I put myself in the admin's position first, then the rider's. Doing it this way helped me understand what each role actually does, how the program should behave when it starts, and why the two menus need to be completely separate.

The system constraints part was a bit tricky at first. The problem was that some of the functional requirements felt like they belonged in the constraints section and the other way around, so the two were getting mixed up in my head. I ended up separating them myself before writing anything down, which made it much easier to organise my thoughts and then put everything into the markdown file.

By the end of the session I had the purpose written, the functional requirements analysed, and the system constraints done and separated clearly.

#### 21.03.2026 Creating IPO and Research

The first thing I did today was the IPO table. I started with the simpler and more obvious features things like rider menu choices, admin menu choices, and similar obvius inputs because I already had experience building an IPO table from Part 1. That made the whole process feel a lot more comfortable this time. I gradually extended it and ended up with around 26–27 features, which is a big jump from the first milestone 1  Part 1 where I only had about 8. Looking at that difference actually made me realise that I developed my analysing skills compared to when I started.

After finishing the IPO table I had a decision to make start the algorithm design or do the research first. I went with research because I felt that doing it after the design stage would not be really useful. The whole point of research is to inform my design decisions, so it made more sense to do it before algorithms.

I went through quite a lot of programs while looking for good sources. I started with simple ticket management apps for mobile phones and worked my way up to airline booking systems, but in the end I decided to keep TfL as one of my sources. I think it fits the project better than anything else I found. The difference this time compared to Part 1 is that I was looking at it from a completely different angle instead of focusing on fare calculation or something like that, I was looking specifically at how they handle profile-linked data for each rider and the save-before-exit pattern, both of which I will definitely use to use in my design.

For the second source I wanted to pick something new rather than reuse another source from Part 1. I chose Splitwise, a mobile application for splitting expenses. What stood out to me about it, was the menu-driven structure where every function is clearly separated and accessible from the top-level menu, which is exactly the kind of navigation design I want to follow in CityRide Lite Part 2. I added screenshots of both sources to the markdown file to complete the research section.

#### 22.03.2026 Algorithms and Class diagram

Today was definitely the most challenging day so far. I spent the whole session working on the algorithm design and the class diagram, and both of them turned out to be more complex than I expected.

For the flowcharts I ended up with 10 in total, which might seem like a lot, but I genuinely could not find a better way to cover everything clearly. I put a lot of effort into the decomposition I separated the admin and rider flows straight away because their roles and responsibilities in the program are completely different, so it made no sense to mix them together.

On the rider, I started with the main menu flowchart showing everything the rider can do in the program. Then I created a separate flowchart for profile management because that is a completely new feature that did not exist in Part 1. After that I went through all the other functions the rider needs to perform. Some of them had similarities with Part 1 so I did not go into too much detail there, but I still created the flowcharts to capture the small changes in logic that came with the new requirements.

For the admin  I did not want to end up with another ten flowcharts, so I was more selective. I created the main admin menu flowchart and included the view config option within it because that one is straightforward enough not to need its own diagram. However, I did create a dedicated flowchart for managing the configuration settings because that part has more complex logic  it involves validating entered values, writing data to file, and handling the overall config management flow, which I felt needed its own diagram to explain properly.

The class diagram was honestly the hardest part of the day. I knew going in that it would be bigger and more complex than what I did for Part 1, but it turned out to be even more complex than I had thought. I created all the classes with their attributes and methods without too much trouble, but when I started drawing the relationships I ran into a layout problem almost every class is connected to at least one other class in some way, and no matter how I arranged them I could not avoid lines crossing over each other. I spent quite a bit of time trying different layouts but could not fully solve it. In the end the best solution I could come up with was to colour-code the classes that had overlapping relationships, so that the tutor can still follow which class connects to which even where the lines cross. It is not perfect but I think it communicates the design clearly enough.

Once the diagrams were done I added all the screenshots  flowcharts and the class diagram to the markdown file. At that point I noticed the Gantt chart was still missing, so I created it and took a screenshot that captures all the tasks with their timelines, making sure every milestone up to the final submission was visible.

------------------------------------------------------------------------------------------------------------------------------
