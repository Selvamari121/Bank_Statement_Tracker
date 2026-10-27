import { useState } from "react";
import { uploadPdf } from "../api/transactionApi";

function PdfUpload({ onUploadSuccess }) {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
    setMessage("");
  };

  const handleUpload = async () => {
    if (!file) {
      setMessage("Please select a PDF file.");
      return;
    }

    try {
      setLoading(true);
      setMessage("");

      await uploadPdf(file);

      setMessage("PDF uploaded successfully.");

      setFile(null);

      if (onUploadSuccess) {
        onUploadSuccess();
      }
    } catch (error) {
      console.error(error);
      setMessage("Failed to upload PDF.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-card">
      <h2>Upload Bank Statement</h2>

      <div className="upload-row">
        <input
          type="file"
          accept=".pdf,application/pdf"
          onChange={handleFileChange}
        />

        <button
          type="button"
          onClick={handleUpload}
          disabled={loading}
        >
          {loading ? "Uploading..." : "Upload PDF"}
        </button>
      </div>

      {file && (
        <p className="file-name">
          Selected: {file.name}
        </p>
      )}

      {message && (
        <p className="message">
          {message}
        </p>
      )}
    </div>
  );
}

export default PdfUpload;