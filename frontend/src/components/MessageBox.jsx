import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';
function normalizeMarkdown(s) {
  if (!s) return '';
  let out = String(s);

  // Strip outer quotes if the entire content is quoted
  out = out.replace(/^\s*"(.*)"\s*$/s, '$1');

  // Normalize CRLF and stray \r
  out = out.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  // Normalize smart quotes and NBSP
  out = out.replace(/[""]/g, '"').replace(/[``]/g, "'").replace(/\u00A0/g, ' ');

  out = out.replace(/\\\[([^\]]*[\\/](?:times|frac|gamma|text|omega|vec)[^\]]*)\\\]/g, '$ $1 $');

  out = out.replace(/\\\(([^\)]*[\\/](?:times|frac|gamma|text|omega|vec)[^\)]*)\\\)/g, '$ $1 $');

  return out.trim();
}
function MessageBox({ message }) {
    const text = normalizeMarkdown(message.text);

    return (
        <div className={`my-2 p-3 rounded-2xl max-w-[85%] shadow-sm self-start ${message.isUser ? 'ml-auto bg-gradient-to-r from-yellow-400 to-yellow-300 text-black' : 'bg-[#101214] text-yellow-200 border border-yellow-900'}`}>
            <div className='markdown prose prose-invert max-w-none'>
                <ReactMarkdown remarkPlugins={[remarkGfm, remarkMath]} rehypePlugins={[rehypeKatex]}>{text}</ReactMarkdown>
            </div>
        </div>
    );
}

export default React.memo(MessageBox, (prevProps, nextProps) => {
    // shallow compare message identity/values - if unchanged, skip re-render
    return prevProps.message === nextProps.message || (
        prevProps.message?.text === nextProps.message?.text && prevProps.message?.isUser === nextProps.message?.isUser
    );
});