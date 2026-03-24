import React from 'react';
export { apiClient } from './api';

export const Button = ({ children }: { children: React.ReactNode }) => {
  return <button style={{ padding: '10px', background: 'blue', color: 'white' }}>{children}</button>;
};