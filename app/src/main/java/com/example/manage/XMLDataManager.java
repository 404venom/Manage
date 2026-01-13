package com.example.manage;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class XMLDataManager {
    private String xmlFilePath;
    private Document document;
    private XPath xpath;

    public XMLDataManager(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
        this.xpath = XPathFactory.newInstance().newXPath();
        loadXML();
    }

    private void loadXML() {
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                createDefaultXML();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDefaultXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = document.createElement("data");
            document.appendChild(root);

            saveXML();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== AUTHENTICATION ====================

    public User authenticateUser(String username, String password, String role) {
        try {
            String expression = String.format(
                    "//user[username='%s' and password='%s' and role='%s']",
                    username, password, role
            );
            Node userNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (userNode != null) {
                return parseUserNode(userNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== TASK OPERATIONS ====================

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            String expression = "//task";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                tasks.add(parseTaskNode(nodeList.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public Task getTaskById(String taskId) {
        try {
            String expression = "//task[id='" + taskId + "']";
            Node taskNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (taskNode != null) {
                return parseTaskNode(taskNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Task> getTasksByEmployee(String employeeId) {
        List<Task> tasks = new ArrayList<>();
        try {
            String expression = "//task[assignedTo='" + employeeId + "']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                tasks.add(parseTaskNode(nodeList.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<>();
        try {
            String expression = "//task[status='" + status + "']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                tasks.add(parseTaskNode(nodeList.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getTasksByPriority(String priority) {
        List<Task> tasks = new ArrayList<>();
        try {
            String expression = "//task[priority='" + priority + "']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                tasks.add(parseTaskNode(nodeList.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getRecentTasks(int limit) {
        List<Task> allTasks = getAllTasks();
        // Trier par date de création (décroissant)
        allTasks.sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()));

        return allTasks.subList(0, Math.min(limit, allTasks.size()));
    }

    public List<Task> getUnassignedTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            String expression = "//task[not(assignedTo) or assignedTo='']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                tasks.add(parseTaskNode(nodeList.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean addTask(Task task) {
        try {
            Element root = document.getDocumentElement();
            Element tasksRoot = getOrCreateElement(root, "tasks");
            Element taskElement = document.createElement("task");

            appendChild(taskElement, "id", task.getId());
            appendChild(taskElement, "title", task.getTitle());
            appendChild(taskElement, "description", task.getDescription());
            appendChild(taskElement, "priority", task.getPriority());
            appendChild(taskElement, "status", task.getStatus());
            appendChild(taskElement, "assignedTo", task.getAssignedTo());
            appendChild(taskElement, "createdBy", task.getCreatedBy());
            appendChild(taskElement, "dueDate", task.getDueDate());
            appendChild(taskElement, "createdDate", task.getCreatedDate());

            tasksRoot.appendChild(taskElement);
            saveXML();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTaskStatus(String taskId, String newStatus) {
        try {
            String expression = "//task[id='" + taskId + "']/status";
            Node statusNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (statusNode != null) {
                statusNode.setTextContent(newStatus);
                saveXML();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTask(String taskId) {
        try {
            String expression = "//task[id='" + taskId + "']";
            Node taskNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (taskNode != null) {
                taskNode.getParentNode().removeChild(taskNode);
                saveXML();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignTaskToEmployee(String taskId, String employeeId) {
        try {
            String expression = "//task[id='" + taskId + "']/assignedTo";
            Node assignedToNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (assignedToNode != null) {
                assignedToNode.setTextContent(employeeId);
            } else {
                // Créer le nœud si inexistant
                String taskExpression = "//task[id='" + taskId + "']";
                Node taskNode = (Node) xpath.evaluate(taskExpression, document, XPathConstants.NODE);
                if (taskNode != null) {
                    Element assignedToElement = document.createElement("assignedTo");
                    assignedToElement.setTextContent(employeeId);
                    taskNode.appendChild(assignedToElement);
                }
            }

            saveXML();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== TASK STATISTICS ====================

    public int getTotalTasksCount() {
        return getAllTasks().size();
    }

    public int getTaskCountByStatus(String status) {
        return getTasksByStatus(status).size();
    }

    public int getTaskCountByPriority(String priority) {
        return getTasksByPriority(priority).size();
    }

    public int getTaskCountByEmployee(String employeeId) {
        return getTasksByEmployee(employeeId).size();
    }

    public int getCompletedTaskCountByEmployee(String employeeId) {
        List<Task> tasks = getTasksByEmployee(employeeId);
        int count = 0;
        for (Task task : tasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getTaskCountByCreator(String creatorId) {
        try {
            String expression = "//task[createdBy='" + creatorId + "']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            return nodeList.getLength();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTaskCountByPeriod(String period) {
        List<Task> allTasks = getAllTasks();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        int count = 0;
        for (Task task : allTasks) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date createdDate = sdf.parse(task.getCreatedDate());

                long diffInMillis = now.getTime() - createdDate.getTime();
                long days = diffInMillis / (1000 * 60 * 60 * 24);

                switch (period) {
                    case "WEEKLY":
                        if (days <= 7) count++;
                        break;
                    case "MONTHLY":
                        if (days <= 30) count++;
                        break;
                    case "YEARLY":
                        if (days <= 365) count++;
                        break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public int getCompletedTaskCountByPeriod(String period) {
        List<Task> completedTasks = getTasksByStatus("COMPLETED");
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        int count = 0;
        for (Task task : completedTasks) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date createdDate = sdf.parse(task.getCreatedDate());

                long diffInMillis = now.getTime() - createdDate.getTime();
                long days = diffInMillis / (1000 * 60 * 60 * 24);

                switch (period) {
                    case "WEEKLY":
                        if (days <= 7) count++;
                        break;
                    case "MONTHLY":
                        if (days <= 30) count++;
                        break;
                    case "YEARLY":
                        if (days <= 365) count++;
                        break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public int getPendingTaskCountByPeriod(String period) {
        return getTaskCountByPeriod(period) - getCompletedTaskCountByPeriod(period);
    }

    public int getCancelledTaskCountByPeriod(String period) {
        List<Task> cancelledTasks = getTasksByStatus("CANCELLED");
        // Similaire à getCompletedTaskCountByPeriod mais pour CANCELLED
        return 0; // Simplification
    }

    public double getAverageCompletionTime(String period) {
        List<Task> completedTasks = getTasksByStatus("COMPLETED");
        if (completedTasks.isEmpty()) return 0.0;

        long totalDays = 0;
        int count = 0;

        for (Task task : completedTasks) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date createdDate = sdf.parse(task.getCreatedDate());
                Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(task.getDueDate());

                long diffInMillis = dueDate.getTime() - createdDate.getTime();
                long days = diffInMillis / (1000 * 60 * 60 * 24);

                totalDays += days;
                count++;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return count > 0 ? (double) totalDays / count : 0.0;
    }

    // ==================== EMPLOYEE OPERATIONS ====================

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try {
            String expression = "//user[role='EMPLOYEE']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Employee employee = parseEmployeeNode(nodeList.item(i));

                // Charger les statistiques de tâches
                int totalTasks = getTaskCountByEmployee(employee.getId());
                int completedTasks = getCompletedTaskCountByEmployee(employee.getId());
                employee.updateTaskCounts(totalTasks - completedTasks, completedTasks);

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employees;
    }

    public int getTotalEmployeesCount() {
        return getAllEmployees().size();
    }

    public int getActiveEmployeesCount() {
        List<Employee> employees = getAllEmployees();
        int count = 0;
        for (Employee employee : employees) {
            if (employee.isActive()) {
                count++;
            }
        }
        return count;
    }

    // ==================== USER OPERATIONS ====================

    public boolean updateUser(User user) {
        try {
            String expression = "//user[id='" + user.getId() + "']";
            Node userNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (userNode != null) {
                updateNodeChild(userNode, "phone", user.getPhone());
                updateNodeChild(userNode, "department", user.getDepartment());
                updateNodeChild(userNode, "password", user.getPassword());

                saveXML();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== NOTIFICATION OPERATIONS ====================

    public List<Notification> getNotificationsByUser(String userId) {
        List<Notification> notifications = new ArrayList<>();
        try {
            String expression = "//notification[userId='" + userId + "']";
            NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                notifications.add(parseNotificationNode(nodeList.item(i)));
            }

            // Trier par date (plus récent en premier)
            notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public boolean updateNotification(Notification notification) {
        try {
            String expression = "//notification[id='" + notification.getId() + "']/read";
            Node readNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (readNode != null) {
                readNode.setTextContent(String.valueOf(notification.isRead()));
                saveXML();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteNotification(String notificationId) {
        try {
            String expression = "//notification[id='" + notificationId + "']";
            Node notificationNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (notificationNode != null) {
                notificationNode.getParentNode().removeChild(notificationNode);
                saveXML();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== PARSING METHODS ====================

    private Task parseTaskNode(Node node) {
        Task task = new Task();
        try {
            task.setId(xpath.evaluate("id", node));
            task.setTitle(xpath.evaluate("title", node));
            task.setDescription(xpath.evaluate("description", node));
            task.setPriority(xpath.evaluate("priority", node));
            task.setStatus(xpath.evaluate("status", node));
            task.setAssignedTo(xpath.evaluate("assignedTo", node));
            task.setCreatedBy(xpath.evaluate("createdBy", node));
            task.setDueDate(xpath.evaluate("dueDate", node));
            task.setCreatedDate(xpath.evaluate("createdDate", node));

            // Récupérer les noms
            String assignedToId = task.getAssignedTo();
            if (assignedToId != null && !assignedToId.isEmpty()) {
                User assignedUser = getUserById(assignedToId);
                if (assignedUser != null) {
                    task.setAssignedToName(assignedUser.getFullName());
                }
            }

            String createdById = task.getCreatedBy();
            if (createdById != null && !createdById.isEmpty()) {
                User creator = getUserById(createdById);
                if (creator != null) {
                    task.setCreatedByName(creator.getFullName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return task;
    }

    private User parseUserNode(Node node) {
        User user = new User();
        try {
            user.setId(xpath.evaluate("id", node));
            user.setUsername(xpath.evaluate("username", node));
            user.setPassword(xpath.evaluate("password", node));
            user.setFullName(xpath.evaluate("fullName", node));
            user.setEmail(xpath.evaluate("email", node));
            user.setPhone(xpath.evaluate("phone", node));
            user.setRole(xpath.evaluate("role", node));
            user.setDepartment(xpath.evaluate("department", node));
            user.setJoinDate(xpath.evaluate("joinDate", node));
            user.setActive(Boolean.parseBoolean(xpath.evaluate("active", node)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private Employee parseEmployeeNode(Node node) {
        Employee employee = new Employee();
        try {
            employee.setId(xpath.evaluate("id", node));
            employee.setUsername(xpath.evaluate("username", node));
            employee.setPassword(xpath.evaluate("password", node));
            employee.setFullName(xpath.evaluate("fullName", node));
            employee.setEmail(xpath.evaluate("email", node));
            employee.setPhone(xpath.evaluate("phone", node));
            employee.setDepartment(xpath.evaluate("department", node));
            employee.setJoinDate(xpath.evaluate("joinDate", node));
            employee.setActive(Boolean.parseBoolean(xpath.evaluate("active", node)));
            employee.setSupervisorId(xpath.evaluate("supervisorId", node));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employee;
    }

    private Notification parseNotificationNode(Node node) {
        Notification notification = new Notification();
        try {
            notification.setId(xpath.evaluate("id", node));
            notification.setUserId(xpath.evaluate("userId", node));
            notification.setTaskId(xpath.evaluate("taskId", node));
            notification.setType(xpath.evaluate("type", node));
            notification.setTitle(xpath.evaluate("title", node));
            notification.setMessage(xpath.evaluate("message", node));
            notification.setTimestamp(xpath.evaluate("timestamp", node));
            notification.setRead(Boolean.parseBoolean(xpath.evaluate("read", node)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notification;
    }

    private User getUserById(String userId) {
        try {
            String expression = "//user[id='" + userId + "']";
            Node userNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (userNode != null) {
                return parseUserNode(userNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== UTILITY METHODS ====================

    private void appendChild(Element parent, String tagName, String value) {
        Element element = document.createElement(tagName);
        element.setTextContent(value != null ? value : "");
        parent.appendChild(element);
    }

    private Element getOrCreateElement(Element parent, String tagName) {
        NodeList children = parent.getElementsByTagName(tagName);
        if (children.getLength() > 0) {
            return (Element) children.item(0);
        } else {
            Element element = document.createElement(tagName);
            parent.appendChild(element);
            return element;
        }
    }

    private void updateNodeChild(Node parent, String childName, String newValue) {
        try {
            String expression = childName;
            Node childNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);

            if (childNode != null) {
                childNode.setTextContent(newValue);
            } else {
                Element childElement = document.createElement(childName);
                childElement.setTextContent(newValue);
                parent.appendChild(childElement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveXML() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}