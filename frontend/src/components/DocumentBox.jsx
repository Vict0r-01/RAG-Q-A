import React from 'react';
const DocumentBox = ({ document, onRemove }) => {
  return (
    <div className="relative flex items-center gap-3 bg-[#0d0d0d] border border-yellow-800 text-yellow-200 rounded-full px-3 py-2 shadow-sm">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
          d="m19.638 8.945c0-.263-.093-.504-.249-.692l.001.002-.004-.006c-.02-.024-.041-.047-.063-.069l-.009-.009c-.009-.009-.019-.02-.028-.029l-7.823-7.821c-.022-.022-.046-.044-.07-.063l-.022-.02c-.018-.014-.036-.027-.054-.04l-.027-.018-.058-.036-.024-.014c-.027-.015-.054-.027-.081-.039l-.033-.013-.057-.021-.037-.012-.067-.017-.026-.006c-.031-.006-.063-.011-.094-.015h-.027c-.027 0-.053 0-.08 0h-9.613c-.6 0-1.087.485-1.091 1.084v21.818c0 .603.489 1.091 1.091 1.091h17.454c.603 0 1.091-.489 1.091-1.091v-13.935c0-.009.001-.019.001-.029zm-7.857-5.225 4.13 4.135h-4.13zm-9.599 18.098v-19.637h7.42v6.767c0 .603.489 1.091 1.091 1.091h6.767v11.779z" />
        </svg>
        <h2 className="text-sm font-medium max-w-xs truncate">{document.title}</h2>
        <button
          type="button"
          aria-label="remove document"
          className='ml-2 bg-yellow-500 text-black rounded-full w-6 h-6 flex items-center justify-center hover:brightness-60'
          onClick={() => onRemove(document.id)}
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
    </div>
  );
};

export default React.memo(DocumentBox, (prevProps, nextProps) => {
  return prevProps.document === nextProps.document || (
    prevProps.document?.title === nextProps.document?.title &&
    prevProps.document?.id === nextProps.document?.id
  );
});