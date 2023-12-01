import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import Api from '../../../apis/api';

function Page() {
  const meQuery = Api.Auth.Query.useMeQuery();
  const navigate = useNavigate();
  const loginMutation = Api.Auth.Mutation.useLoginMutation();
  const { register, handleSubmit } = useForm<Api.Auth.Dto.Login>();

  if (meQuery.data) {
    navigate('/');
  }

  return (
    <div className="mx-auto flex w-full max-w-[1200px] flex-col gap-24 py-25">
      <h2 className="text-32 font-[600] text-[#111827]">Sign In</h2>
      <form
        className="flex w-full flex-col gap-24"
        onSubmit={handleSubmit((data) => {
          loginMutation.mutate(data, {
            onSuccess: () => {
              toast.success('Succeed !');
            },
            onError: (error) => {
              toast.error(error?.response?.data?.message);
            },
          });
        })}
      >
        <label className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Username</p>
          <input
            {...register('username')}
            type="text"
            placeholder="Please Input Title"
            className="w-200 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
          />
        </label>
        <label className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Password</p>
          <input
            {...register('password')}
            type="password"
            placeholder="Please Input Password"
            className="w-200 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
          />
        </label>
        <label className="flex items-center gap-8">
          <input type="checkbox" defaultChecked />
          <p className="text-14 font-[400] text-[#111827]">Remember me</p>
        </label>
        <div className="flex items-center gap-24">
          <button
            type="submit"
            className="cursor-pointer rounded-6 border-none bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] transition-all hover:bg-[#6366F1]"
          >
            Login
          </button>
          <Link
            to="/auth/sign-up"
            className="text-14 font-[600] text-[#111817] no-underline"
          >
            Sign up
          </Link>
        </div>
      </form>
    </div>
  );
}

export default Page;
