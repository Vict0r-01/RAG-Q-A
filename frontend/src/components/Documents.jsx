import DocumentBox from "./DocumentBox";
import React from "react";
const Documents = ({documents = [], onRemove}) => {

  return (
    <div className="flex flex-wrap gap-3 w-full max-h-60 overflow-x-auto p-2 items-center">
        {documents.map((doc, index) => (
            <DocumentBox key={index} document={doc} onRemove={onRemove} />
        ))}
    </div>
  );
}

export default React.memo(Documents, (prevProps, nextProps) => {
  // shallow compare array reference - if parent keeps same reference, no re-render
  return prevProps.documents === nextProps.documents;
});