import { useEffect, useState } from "react";
import PdfUpload from "../components/PdfUpload";
import MemberTable from "../components/MemberTable";
import { getSummary } from "../api/transactionApi";

function Dashboard() {
  const [summary, setSummary] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadSummary = async () => {
    try {
      setLoading(true);

      const data = await getSummary();

      setSummary(data);
    } catch (error) {
      console.error("Failed to load summary:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSummary();
  }, []);

  return (
    <div className="dashboard">
      <div className="page-header">
        <h1>Bank Statement Tracker</h1>

        <p>
          Upload your bank statement and view
          member-wise credit and debit totals.
        </p>
      </div>

      <PdfUpload onUploadSuccess={loadSummary} />

      {loading ? (
        <div className="loading">
          Loading transactions...
        </div>
      ) : (
        <MemberTable data={summary} />
      )}
    </div>
  );
}

export default Dashboard;