import 'react-quill/dist/quill.snow.css';

import React from 'react';
import dayjs, { Dayjs } from 'dayjs';
import { useNavigate, useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import Api from '../../../apis/api';
import useNow from '../../../hooks/useNow';
import skipSound from '../../../assets/sounds/skip.mp3';

interface Chat extends Api.Room.Dto.ChatResponse {
  createdAt: string;
}

interface Question extends Api.Room.Dto.QuestionResponse {
  createdAt: Dayjs;
}

function Page() {
  const params = useParams();
  const id = Number(params.id);
  const meQuery = Api.Auth.Query.useMeQuery();
  const roomQuery = Api.Room.Query.useRoomQuery(id);
  const clientRef = React.useRef<Api.Room.WebSocket.Client>();
  const navigate = useNavigate();
  const [chats, setChats] = React.useState<Chat[]>([]);
  const [question, setQuestion] = React.useState<Question>();
  const { register, reset, handleSubmit } = useForm<{ message: string }>({
    defaultValues: {
      message: '',
    },
  });
  const queryClient = useQueryClient();
  const now = useNow();
  const duration = React.useMemo(() => {
    if (!question) {
      return null;
    }
    return dayjs.duration(
      question.createdAt.add(question.timeoutMs ?? 0, 'ms').diff(now),
    );
  }, [question, now]);
  const [hints, setHints] = React.useState<Api.Room.Dto.HintResponse[]>([]);
  const [answers, setAnswers] = React.useState<Api.Room.Dto.AnswerResponse>();

  React.useEffect(() => {
    clientRef.current = Api.Room.WebSocket.join(Number(id), {
      onChat(data) {
        setChats((prevState) => [
          ...prevState,
          { ...data, createdAt: dayjs().toISOString() },
        ]);
      },
      onQuestion(data) {
        setQuestion({ ...data, createdAt: dayjs() });
        setHints([]);
        setAnswers(undefined);
        queryClient.invalidateQueries({
          queryKey: [Api.Room.Query.QUERY_KEY.ROOM],
        });
      },
      onHint(hint) {
        setHints((prevState) => {
          return [...prevState, hint];
        });
      },
      onAnswer(answer) {
        setAnswers(answer);
        queryClient.invalidateQueries({
          queryKey: [Api.Room.Query.QUERY_KEY.ROOM],
        });
      },
      onSkip() {
        const audio = new Audio(skipSound);
        audio.play();
      },
    });
    return () => {
      clientRef.current?.disconnect();
    };
  }, []);

  if (!meQuery.data) {
    navigate('/auth/sign-in');
    return null;
  }

  if (!roomQuery.data) {
    return null;
  }

  return (
    <div className="mx-auto flex w-full max-w-[1200px] flex-col gap-12 py-25">
      <span className="text-14 font-[400] text-[#64748B]">
        {roomQuery.data.data.roomQuizCategories
          .map((response) => {
            return response.quizCategory.name;
          })
          .join(',')}
      </span>
      <div className="flex items-center justify-between">
        <h2 className="text-32 font-[600] text-[#111827]">
          {roomQuery.data.data.title} ({roomQuery.data.data.status}) (
          {roomQuery.data.data.currentNumber}/{roomQuery.data.data.numberOfQuiz}
          )
        </h2>
        <div className="flex items-center gap-12">
          <button
            type="button"
            className="cursor-pointer rounded-6 border-none bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] transition-all hover:bg-[#6366F1]"
            onClick={clientRef.current?.play}
          >
            Play
          </button>
          <button
            type="button"
            className="cursor-pointer rounded-6 border-none bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] transition-all hover:bg-[#6366F1]"
            onClick={clientRef.current?.skip}
          >
            Skip
          </button>
        </div>
      </div>
      <div className="flex gap-12">
        <div className="flex w-full flex-col gap-12">
          <div className="flex h-350 flex-col items-center justify-center gap-12 rounded-6 border border-solid border-[#E5E7EB] p-24">
            {question ? (
              <React.Fragment
                key={`Question-${question.createdAt.toISOString()}`}
              >
                {/* eslint-disable-next-line jsx-a11y/media-has-caption */}
                <audio controls autoPlay className="hidden" loop>
                  <source
                    src={Api.File.getFilePath(question.soundFile.id)}
                    type={question.soundFile.contentType}
                  />
                </audio>
                <div className="flex flex-col items-center justify-center gap-12">
                  <p dangerouslySetInnerHTML={{ __html: question.content }} />
                  <p className="text-center text-14 font-[400] text-[#111827]">
                    Remaining time:{' '}
                    {Math.max(Math.floor(duration?.asSeconds() ?? 0), 0)}
                  </p>
                  {hints.map((hint) => {
                    return (
                      <p
                        key={`Hint-${hint.id}`}
                        className="text-center text-14 font-[400] text-[#111827]"
                      >
                        {hint.name}: {hint.content}
                      </p>
                    );
                  })}
                  {answers && (
                    <p className="text-center text-14 font-[400] text-[#111827]">
                      정답: {answers.answers.join(',')}
                    </p>
                  )}
                </div>
              </React.Fragment>
            ) : roomQuery.data.data.status === Api.Room.Status.PLAYING ? (
              'Please wait...'
            ) : (
              'READY'
            )}
          </div>
          <ul className="flex h-250 flex-col justify-end gap-4 overflow-y-hidden rounded-6 border border-solid border-[#E5E7EB] p-8">
            {chats.map((chat, chatIndex) => {
              return (
                <li
                  key={`Chat-${chatIndex}`}
                  className="text-14 font-[600] text-[#111827]"
                >
                  [{dayjs(chat.createdAt).format('HH:mm:ss')}] {chat.user.name}:{' '}
                  {chat.message}
                  {chat.isCorrect && (
                    <span className="text-[#FF0000]"> (정답)</span>
                  )}
                </li>
              );
            })}
          </ul>
          <form
            onSubmit={handleSubmit((data) => {
              if (!(data.message.length > 0)) {
                toast.warning('Please Input Message !');
                return;
              }
              reset();
              clientRef.current?.chat({
                roomId: id,
                accountId: meQuery.data.data.id,
                message: data.message,
              });
            })}
            className="flex w-full items-center gap-12"
          >
            <span className="text-14 font-[400] text-[#111827]">
              {meQuery.data.data.name}
            </span>
            <input
              {...register('message')}
              type="text"
              placeholder="Please Input Message"
              className="flex-1 appearance-none rounded-6 border border-solid border-[#E5E7EB] px-12 py-8 text-14 font-[400] text-[#111827]"
            />
            <button
              type="submit"
              className="cursor-pointer rounded-6 border-none bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] transition-all hover:bg-[#6366F1]"
            >
              Send
            </button>
          </form>
        </div>
        <div className="flex h-300 basis-350 flex-col gap-12 overflow-y-auto rounded-6 border border-solid border-[#E5E7EB] p-24">
          <p className="text-14 font-[400] text-[#64748B]">Rank</p>
          {roomQuery.data.data.users.map((user) => {
            return (
              <p
                key={`User-${user.user.id}`}
                className="text-14 font-[400] text-[#111827]"
              >
                {user.rank}. {user.user.name}: {user.count}
              </p>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default Page;
