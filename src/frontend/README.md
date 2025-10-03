# SAVED 4

A collaborative platform designed by six University of Twente bachelor student
---

## Deployment

**Link to Deployment:** [Your Deployment URL Here](https://your-deployment-url.com)

**Login Credentials:**

To test the deployment, you can use the following credentials:

* **Admin User:**
    * **Username:** `admin@example.com`
    * **Password:** `adminpassword123`

* **Company User:**
    * **Username:** `company@example.com`
    * **Password:** `companypassword123`

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

---