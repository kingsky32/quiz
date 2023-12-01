import React from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useForm } from 'react-hook-form';
import Api from '../../../apis/api';

function Page() {
  const navigate = useNavigate();
  const quizCategoriesQuery = Api.QuizCategory.Query.useQuizCategoriesQuery();
  const createRoomMutation = Api.Room.Mutation.useCreateRoomMutation();
  const { register, handleSubmit, watch } = useForm<Api.Room.Dto.Create>();

  return (
    <div className="mx-auto flex w-full max-w-[1200px] flex-col gap-24 py-25">
      <h2 className="text-32 font-[600] text-[#111827]">Create Room</h2>
      <form
        className="flex w-full flex-col gap-24"
        onSubmit={handleSubmit((data) => {
          createRoomMutation.mutate(
            {
              ...data,
              numberOfQuiz: Number(data.numberOfQuiz),
              quizCategoryIds: data.quizCategoryIds.map(Number),
            },
            {
              onSuccess: (axiosResponse) => {
                toast.success('Created !');
                navigate(`/room/${axiosResponse.data.id}`);
              },
              onError: (error) => {
                toast.error(error?.response?.data?.message);
              },
            },
          );
        })}
      >
        <label className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Title</p>
          <input
            {...register('title')}
            type="text"
            placeholder="Please Input Title"
            className="w-200 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
          />
        </label>
        <label className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Number of Quiz</p>
          <input
            {...register('numberOfQuiz')}
            type="number"
            min={0}
            placeholder="Count"
            className="w-120 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
          />
        </label>
        <div className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Secret</p>
          <div className="flex items-center gap-12">
            <label className="flex items-center gap-8">
              <input {...register('isSecret')} type="checkbox" />
              <p className="text-14 font-[400] text-[#111827]">Secret</p>
            </label>
            <input
              {...register('secretPassword')}
              type="password"
              placeholder="Please Input Password"
              className="w-200 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
              disabled={!watch('isSecret')}
            />
          </div>
        </div>
        <label className="flex flex-col gap-8">
          <p className="text-14 font-[400] text-[#111827]">Quiz Category</p>
          <select
            {...register('quizCategoryIds')}
            placeholder="Select Quiz Category"
            className="w-200 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
            multiple
          >
            {(() => {
              const arr: React.ReactNode[] = [];
              function recursive(
                quizCategory: Api.QuizCategory.Dto.Response,
                name: string,
              ) {
                if (quizCategory.children) {
                  quizCategory.children.forEach((response) => {
                    recursive(response, `${name}/${response.name}`);
                  });
                } else {
                  arr.push(
                    <option
                      key={`Quiz-Category-${quizCategory.id}`}
                      value={quizCategory.id}
                    >
                      {name}
                    </option>,
                  );
                }
              }
              quizCategoriesQuery.data?.data.forEach((response) => {
                recursive(response, response.name);
              });
              return arr;
            })()}
          </select>
        </label>
        <div>
          <button
            type="submit"
            className="cursor-pointer rounded-6 border-none bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] transition-all hover:bg-[#6366F1]"
          >
            Create
          </button>
        </div>
      </form>
    </div>
  );
}

export default Page;
