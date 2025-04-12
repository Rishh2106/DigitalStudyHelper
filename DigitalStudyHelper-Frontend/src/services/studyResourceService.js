import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export const createStudyResource = async (resourceData) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/create-resource`, resourceData);
        return response.data;
    } catch (error) {
        console.error('Error creating study resource:', error);
        throw error;
    }
};

export const getStudyResources = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/resources`);
        return response.data;
    } catch (error) {
        console.error('Error fetching study resources:', error);
        throw error;
    }
}; 