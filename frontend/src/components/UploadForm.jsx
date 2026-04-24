import { useState } from "react";

function UploadForm({ onUpload, uploading }) {
  const [fileName, setFileName] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();

    const fileInput = event.target.elements.file;
    const file = fileInput.files?.[0];

    if (file) {
      onUpload(file);
    }
  };

  return (
    <form className="upload-form" onSubmit={handleSubmit}>
      <label className="upload-zone">
        <input
          name="file"
          type="file"
          accept=".pdf,.txt,.doc,.docx,.ppt,.pptx"
          onChange={(event) => setFileName(event.target.files?.[0]?.name || "")}
          required
        />

        <span>Upload Study Material</span>

        <small>
          Supported files: PDF, TXT, DOC, DOCX, PPT, PPTX
        </small>
        {fileName && <small className="file-label">Selected: {fileName}</small>}
      </label>

      <button
        type="submit"
        className="btn"
        disabled={uploading}
      >
        {uploading ? "Uploading..." : "Upload Document"}
      </button>
    </form>
  );
}

export default UploadForm;