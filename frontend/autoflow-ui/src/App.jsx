import { useEffect, useState } from "react";
import {
  createWorkflow,
  getWorkflows,
  executeWorkflow,
  getTasks,
  getLogs,
  retryTask,
  deleteWorkflow,
} from "./api/workflowApi";

function App() {
  const [workflows, setWorkflows] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [logs, setLogs] = useState([]);

  const [formData, setFormData] = useState({
    employeeName: "",
    employeeEmail: "",
    department: "",
    designation: "",
  });

  const loadWorkflows = async () => {
    try {
      const res = await getWorkflows();
      setWorkflows(res.data);
    } catch (error) {
      console.error("Error loading workflows:", error);
      alert("Failed to load workflows. Is backend running?");
    }
  };

  useEffect(() => {
    loadWorkflows();
  }, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleCreate = async (e) => {
    e.preventDefault();

    try {
      await createWorkflow(formData);
      setFormData({
        employeeName: "",
        employeeEmail: "",
        department: "",
        designation: "",
      });
      loadWorkflows();
      alert("Workflow created successfully");
    } catch (error) {
      console.error("Error creating workflow:", error);
      alert("Failed to create workflow");
    }
  };

  const handleExecute = async (id) => {
    try {
      await executeWorkflow(id);
      alert("Workflow executed");
      await loadWorkflows();
      if (selectedId === id) {
        await handleSelect(id);
      }
    } catch (error) {
      console.error("Error executing workflow:", error);
      alert("Failed to execute workflow");
    }
  };

  const handleSelect = async (id) => {
    try {
      setSelectedId(id);

      const taskRes = await getTasks(id);
      setTasks(taskRes.data);

      const logRes = await getLogs(id);
      setLogs(logRes.data);
    } catch (error) {
      console.error("Error fetching workflow details:", error);
      alert("Failed to fetch tasks/logs");
    }
  };

  const handleRetry = async (taskId) => {
    try {
      await retryTask(taskId);
      alert("Retry triggered");
      if (selectedId) {
        await handleSelect(selectedId);
      }
      await loadWorkflows();
    } catch (error) {
      console.error("Error retrying task:", error);
      alert("Failed to retry task");
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteWorkflow(id);

      if (selectedId === id) {
        setSelectedId(null);
        setTasks([]);
        setLogs([]);
      }

      await loadWorkflows();
      alert("Workflow deleted successfully");
    } catch (error) {
      console.error("Error deleting workflow:", error);
      alert("Failed to delete workflow");
    }
  };

  const getStatusBadgeStyle = (status) => {
    const base = {
      display: "inline-block",
      padding: "6px 12px",
      borderRadius: "999px",
      fontSize: "12px",
      fontWeight: "700",
      letterSpacing: "0.4px",
    };

    switch (status) {
      case "COMPLETED":
      case "SUCCESS":
        return {
          ...base,
          backgroundColor: "#dcfce7",
          color: "#166534",
        };
      case "FAILED":
      case "ESCALATED":
        return {
          ...base,
          backgroundColor: "#fee2e2",
          color: "#991b1b",
        };
      case "IN_PROGRESS":
      case "RUNNING":
      case "RETRYING":
        return {
          ...base,
          backgroundColor: "#fef3c7",
          color: "#92400e",
        };
      case "PENDING":
      default:
        return {
          ...base,
          backgroundColor: "#e5e7eb",
          color: "#374151",
        };
    }
  };

  const styles = {
    page: {
      minHeight: "100vh",
      background: "linear-gradient(135deg, #f8fafc, #eef2ff)",
      padding: "30px 20px",
      fontFamily: "Arial, sans-serif",
      color: "#111827",
    },
    container: {
      maxWidth: "1150px",
      margin: "0 auto",
    },
    header: {
      textAlign: "center",
      marginBottom: "30px",
    },
    title: {
      fontSize: "48px",
      marginBottom: "8px",
      fontWeight: "800",
      color: "#111827",
    },
    subtitle: {
      fontSize: "18px",
      color: "#6b7280",
      margin: 0,
    },
    card: {
      background: "#ffffff",
      borderRadius: "18px",
      padding: "24px",
      boxShadow: "0 10px 30px rgba(15, 23, 42, 0.08)",
      border: "1px solid #e5e7eb",
      marginBottom: "24px",
    },
    sectionTitle: {
      fontSize: "28px",
      fontWeight: "800",
      marginBottom: "18px",
      color: "#111827",
    },
    formGrid: {
      display: "grid",
      gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
      gap: "16px",
      marginBottom: "20px",
    },
    field: {
      display: "flex",
      flexDirection: "column",
    },
    label: {
      marginBottom: "8px",
      fontSize: "14px",
      fontWeight: "700",
      color: "#374151",
    },
    input: {
      padding: "12px 14px",
      borderRadius: "12px",
      border: "1px solid #d1d5db",
      fontSize: "14px",
      outline: "none",
      background: "#f9fafb",
    },
    primaryButton: {
      background: "#2563eb",
      color: "#ffffff",
      border: "none",
      borderRadius: "12px",
      padding: "12px 18px",
      fontSize: "14px",
      fontWeight: "700",
      cursor: "pointer",
    },
    executeButton: {
      background: "#111827",
      color: "#ffffff",
      border: "none",
      borderRadius: "10px",
      padding: "10px 14px",
      fontWeight: "700",
      cursor: "pointer",
    },
    viewButton: {
      background: "#4f46e5",
      color: "#ffffff",
      border: "none",
      borderRadius: "10px",
      padding: "10px 14px",
      fontWeight: "700",
      cursor: "pointer",
    },
    retryButton: {
      background: "#d97706",
      color: "#ffffff",
      border: "none",
      borderRadius: "10px",
      padding: "10px 14px",
      fontWeight: "700",
      cursor: "pointer",
    },
    deleteButton: {
      background: "#dc2626",
      color: "#ffffff",
      border: "none",
      borderRadius: "10px",
      padding: "10px 14px",
      fontWeight: "700",
      cursor: "pointer",
    },
    workflowGrid: {
      display: "grid",
      gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
      gap: "18px",
    },
    workflowCard: {
      background: "#ffffff",
      borderRadius: "18px",
      padding: "20px",
      border: "1px solid #e5e7eb",
      boxShadow: "0 6px 18px rgba(15, 23, 42, 0.06)",
    },
    workflowTitle: {
      fontSize: "20px",
      fontWeight: "800",
      marginBottom: "14px",
    },
    detailText: {
      margin: "6px 0",
      color: "#374151",
      lineHeight: "1.5",
    },
    actionRow: {
      display: "flex",
      gap: "10px",
      flexWrap: "wrap",
      marginTop: "16px",
    },
    splitGrid: {
      display: "grid",
      gridTemplateColumns: "1fr 1fr",
      gap: "24px",
      marginTop: "24px",
    },
    subCard: {
      background: "#ffffff",
      borderRadius: "18px",
      padding: "20px",
      border: "1px solid #e5e7eb",
      boxShadow: "0 6px 18px rgba(15, 23, 42, 0.06)",
    },
    taskCard: {
      border: "1px solid #e5e7eb",
      borderRadius: "14px",
      padding: "14px",
      marginBottom: "12px",
      background: "#f9fafb",
    },
    logCard: {
      borderLeft: "5px solid #4f46e5",
      borderRadius: "10px",
      padding: "12px 14px",
      marginBottom: "12px",
      background: "#f8fafc",
    },
    smallTitle: {
      fontSize: "22px",
      fontWeight: "800",
      marginBottom: "16px",
    },
    emptyText: {
      color: "#6b7280",
      fontStyle: "italic",
    },
  };

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <div style={styles.header}>
          <h1 style={styles.title}>AutoFlow AI Dashboard</h1>
          <p style={styles.subtitle}>Autonomous Enterprise Workflow Demo</p>
        </div>

        <div style={styles.card}>
          <h2 style={styles.sectionTitle}>Create Workflow</h2>
          <form onSubmit={handleCreate}>
            <div style={styles.formGrid}>
              <div style={styles.field}>
                <label style={styles.label}>Employee Name</label>
                <input
                  type="text"
                  name="employeeName"
                  value={formData.employeeName}
                  onChange={handleChange}
                  required
                  style={styles.input}
                  placeholder="Enter employee name"
                />
              </div>

              <div style={styles.field}>
                <label style={styles.label}>Employee Email</label>
                <input
                  type="email"
                  name="employeeEmail"
                  value={formData.employeeEmail}
                  onChange={handleChange}
                  required
                  style={styles.input}
                  placeholder="Enter employee email"
                />
              </div>

              <div style={styles.field}>
                <label style={styles.label}>Department</label>
                <input
                  type="text"
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  required
                  style={styles.input}
                  placeholder="Enter department"
                />
              </div>

              <div style={styles.field}>
                <label style={styles.label}>Designation</label>
                <input
                  type="text"
                  name="designation"
                  value={formData.designation}
                  onChange={handleChange}
                  required
                  style={styles.input}
                  placeholder="Enter designation"
                />
              </div>
            </div>

            <button type="submit" style={styles.primaryButton}>
              Create Workflow
            </button>
          </form>
        </div>

        <div style={styles.card}>
          <h2 style={styles.sectionTitle}>Workflows</h2>

          {workflows.length === 0 ? (
            <p style={styles.emptyText}>No workflows yet.</p>
          ) : (
            <div style={styles.workflowGrid}>
              {workflows.map((w) => (
                <div key={w.id} style={styles.workflowCard}>
                  <div style={styles.workflowTitle}>Workflow #{w.id}</div>

                  <p style={styles.detailText}>
                    <strong>Name:</strong> {w.employeeName}
                  </p>
                  <p style={styles.detailText}>
                    <strong>Email:</strong> {w.employeeEmail}
                  </p>
                  <p style={styles.detailText}>
                    <strong>Department:</strong> {w.department}
                  </p>
                  <p style={styles.detailText}>
                    <strong>Designation:</strong> {w.designation}
                  </p>

                  <div style={{ marginTop: "10px", marginBottom: "6px" }}>
                    <strong>Status:</strong>{" "}
                    <span style={getStatusBadgeStyle(w.status)}>{w.status}</span>
                  </div>

                  <div style={styles.actionRow}>
                    <button
                      onClick={() => handleExecute(w.id)}
                      style={styles.executeButton}
                    >
                      Execute
                    </button>
                    <button
                      onClick={() => handleSelect(w.id)}
                      style={styles.viewButton}
                    >
                      View Details
                    </button>
                    <button
                      onClick={() => handleDelete(w.id)}
                      style={styles.deleteButton}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {selectedId && (
          <div style={styles.splitGrid}>
            <div style={styles.subCard}>
              <h2 style={styles.smallTitle}>Tasks for Workflow #{selectedId}</h2>

              {tasks.length === 0 ? (
                <p style={styles.emptyText}>No tasks found.</p>
              ) : (
                tasks.map((t) => (
                  <div key={t.id} style={styles.taskCard}>
                    <p style={styles.detailText}>
                      <strong>Task:</strong> {t.taskName}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Agent:</strong> {t.assignedAgent}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Status:</strong>{" "}
                      <span style={getStatusBadgeStyle(t.status)}>{t.status}</span>
                    </p>
                    <p style={styles.detailText}>
                      <strong>Retry Count:</strong> {t.retryCount}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Error:</strong> {t.errorMessage || "None"}
                    </p>

                    {t.status === "FAILED" && (
                      <button
                        onClick={() => handleRetry(t.id)}
                        style={styles.retryButton}
                      >
                        Retry Task
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>

            <div style={styles.subCard}>
              <h2 style={styles.smallTitle}>Audit Logs</h2>

              {logs.length === 0 ? (
                <p style={styles.emptyText}>No logs found.</p>
              ) : (
                logs.map((l) => (
                  <div key={l.id} style={styles.logCard}>
                    <p style={styles.detailText}>
                      <strong>Agent:</strong> {l.agentName}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Action:</strong> {l.action}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Reason:</strong> {l.decisionReason}
                    </p>
                    <p style={styles.detailText}>
                      <strong>Time:</strong> {l.createdAt}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;