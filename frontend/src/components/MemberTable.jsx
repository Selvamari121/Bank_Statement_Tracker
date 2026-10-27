function MemberTable({ data = [] }) {
  return (
    <div className="table-card">
      <div className="table-header">
        <div>
          <h2>Member Summary</h2>
          <p>
            Credit and debit amounts grouped by member
          </p>
        </div>

        <span className="member-count">
          {data.length} Members
        </span>
      </div>

      {data.length === 0 ? (
        <div className="empty-state">
          No member transactions found.
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="member-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Member</th>
                <th>Credit</th>
                <th>Debit</th>
              </tr>
            </thead>

            <tbody>
              {data.map((item, index) => (
                <tr key={`${item.member}-${index}`}>
                  <td>{index + 1}</td>

                  <td className="member-name">
                    {item.member}
                  </td>

                  <td className="credit-amount">
                    ₹
                    {Number(item.credit || 0).toLocaleString(
                      "en-IN",
                      {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      }
                    )}
                  </td>

                  <td className="debit-amount">
                    ₹
                    {Number(item.debit || 0).toLocaleString(
                      "en-IN",
                      {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      }
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default MemberTable;