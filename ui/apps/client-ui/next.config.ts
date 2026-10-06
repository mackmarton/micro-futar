import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: 'export',
  transpilePackages: ['@package/shared-core', '@package/shared-ui'],
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
