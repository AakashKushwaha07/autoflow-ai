import axios from "axios";

const BASE_URL = "http://localhost:8080/api/workflows";

export const createWorkflow = (data) => axios.post(BASE_URL, data);

export const getWorkflows = () => axios.get(BASE_URL);

export const executeWorkflow = (id) => axios.post(`${BASE_URL}/${id}/execute`);

export const getTasks = (id) => axios.get(`${BASE_URL}/${id}/tasks`);

export const getLogs = (id) => axios.get(`${BASE_URL}/${id}/logs`);

export const retryTask = (taskId) => axios.post(`${BASE_URL}/tasks/${taskId}/retry`);

export const deleteWorkflow = (id) => axios.delete(`${BASE_URL}/${id}`);