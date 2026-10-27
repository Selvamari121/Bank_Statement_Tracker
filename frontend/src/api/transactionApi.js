import axios from "axios";

const API_URL = "http://localhost:8081/api/transactions";

export const uploadPdf = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axios.post(
    `${API_URL}/upload`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return response.data;
};

export const getSummary = async () => {
  const response = await axios.get(`${API_URL}/summary`);
  return response.data;
};