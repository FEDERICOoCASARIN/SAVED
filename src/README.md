# SAVED 4

A collaborative platform designed by six University of Twente bachelor student

## 👥 Team

- ⁠Cleo Joe Rheikel Sinuhaji (s3435024)
- ⁠Federico Casarin (s3352706)
- ⁠Made Vindra Wikananda Saddya Wiriawan (s3314251)
- ⁠Kashyp Subramonian (s3365107)
- ⁠Razin Ahmed Jabir (s3340155)
- Renzo Rashesa Arelian (s3458571)

---
## ✨ Features

- Role-based authentication (Admin and Company users)
- Order creation, editing, and deletion
- Chat system between users and admins
- Calendar-based scheduling interface with drag and drop functionality
- Real-time order notifications with read/unread status
- Visualized analytics via charts

---

## 🛠️ Technologies Used

- Frontend: Vue.js 3, Chart.js, Tailwind CSS
- Backend: Java (JAX-RS), PostgreSQL, Maven
- Deployment: Apache Tomcat 10 on Previder

---

## Deployment

**Link to Deployment:** [Your Deployment URL Here](https://your-deployment-url.com)

**Login Credentials:**

To test the deployment, you can use the following credentials:

* **Admin User:**
    * **Username:** `admin`
    * **Password:** `admin`

### Steps to Create a New Company

1.  **Log in as Admin:**
    * Use the Admin credentials (`admin`/`admin`) to log in.

2.  **Navigate to Company Management:**
    * Upon successful login, locate the administration panel or dashboard.
    * Click on the section typically labeled "**Users**,"  in the navigation menu.

3.  **Initiate Company Creation:**
    * Within the company/user management interface, look for a button named "**Add New Company**,"
    * Click this to open the new company creation form.

4.  **Enter Company Details:**
    * Fill in the required fields in the form. Essential details usually include:
        * **Company Name:** The full name of the organization.
        * **Company Username:** A unique identifier for the company's primary login.
        * **Company Location:** An the latitude and longitude company.

5.  **Inspect, Console, Copy the URL**
    * Inspect the page, locate the URL on the object created.
    * Copy the link on the new browser 
    * Finish the registration by entering your desired password.

6. **Navigate to the login page, and log in using your newly created user.**



---

## 📁 Folder Structure

Here's a brief overview of the main folders in this project:
* `api/`: API documentation
* `backend/`: Backend-related folders
    * `backend/src/`: Contains all the source code for the application backend.
        * `backend/src/main/java/auth`: Authentication and Security
        * `backend/src/main/java/dao`: Database queries
        * `backend/src/main/java/dto`: Data Transfer Objects
        * `backend/src/main/java/enums`: Data Transfer Objects
        * `backend/src/main/java/resource`: RESTful API endpoints
        * `backend/src/main/java/resource`: Service Classes; contains business-logic

* `frontend/`: Frontend-related folders
    : Reusable UI components (e.g., but* `src/components/`tons, forms, navigation).
    * `frontend/src/`: Contains all the source code for the application frontend.
        * `frontend/src/assets/icons`: Reusable icons and images
        * `frontend/src/components`: Reusable components  of the program
            * `frontend/src/components/calendar/`: Calendar reusable components
            * `frontend/src/components/charts/`: Charts reusable components
            * `frontend/src/components/Chat/`: Chat reusable components
            * `frontend/src/components/live/`: Live reusable components
            * `frontend/src/components/orders/`: Orders reusable components
            * `frontend/src/components/profile/`: Profile reusable components
            * `frontend/src/components/svg/`: SVG reusable components
            * `frontend/src/components/users/`: Users reusable components
            * `frontend/src/components/vehicles/`: Vehicles reusable components
        * `frontend/src/router`: Dashboard routing
        * `frontend/src/router`: Dashboard routing



* `public/`: Static assets like `index.html`, images, and fonts that are served directly.
* `config/`: Configuration files for the project (e.g., environment variables, build settings).
* `docs/`: Additional documentation, diagrams, or design specifications.
* `tests/`: Unit, integration, or end-to-end tests for the application.

---

## ⚙️ Local Setup Instructions (If deployment failed or for local development)

If you are unable to access the deployed version or wish to run the project locally, please follow these steps:

### Prerequisites

Ensure you have the following installed on your machine:

* [Node.js](https://nodejs.org/en/download/) (LTS version recommended)
* [npm](https://www.npmjs.com/get-npm) (comes with Node.js) or [Yarn](https://yarnpkg.com/getting-started/install)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
    cd your-repo-name
    ```

2.  **Install dependencies:**
    ```bash
    npm install
    # OR
    yarn install
    ```

### Running the Project

1.  **Start the development server:**
    ```bash
    npm start
    # OR
    yarn start
    ```
2.  The application should now be running locally, typically accessible at `http://localhost:3000` (or another port indicated in your console).

3. **Access the front end**


---